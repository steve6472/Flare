package steve6472.volkaniums.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.FrameInfo;
import steve6472.volkaniums.Model3d;
import steve6472.volkaniums.VkBuffer;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.model.LoadedModel;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.SBO;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;
import steve6472.volkaniums.util.MathUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class TestRenderSystem extends RenderSystem
{
    Model3d model3d;

    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);

    public TestRenderSystem(VkDevice device, Pipeline pipeline, Commands commands, VkQueue graphicsQueue)
    {
        super(device, pipeline);

        createModel(commands, graphicsQueue);

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            FlightFrame frame = new FlightFrame();
            frames.add(frame);

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_UBO_TEST.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.uboBuffer = global;

            VkBuffer sbo = new VkBuffer(
                device,
                SBO.BONES.sizeof(),
                4,
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            sbo.map();
            frame.sboBuffer = sbo;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.descriptorSet = descriptorWriter
                    .writeBuffer(0, frame.uboBuffer, stack)
                    .writeBuffer(1, frame.sboBuffer, stack)
                    .build();
            }
        }
    }

    private void createModel(Commands commands, VkQueue graphicsQueue)
    {
        final String PATH = "C:\\Users\\Steve\\Desktop\\model.bbmodel";
        final File file = new File(PATH);

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        JsonElement jsonElement = JsonParser.parseReader(reader);
        DataResult<Pair<LoadedModel, JsonElement>> decode = LoadedModel.CODEC.decode(JsonOps.INSTANCE, jsonElement);

        model3d = new Model3d();
        model3d.createVertexBuffer(device, commands, graphicsQueue, decode.getOrThrow().getFirst().toPrimitiveModel().toVkVertices(), Vertex.POS3F_COL3F_UV);
    }

    @Override
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        FlightFrame flightFrame = frames.get(frameInfo.frameIndex);
        // Update

        var globalUBO = UBO.GLOBAL_UBO_TEST.create(frameInfo.camera.getProjectionMatrix(), new Matrix4f().translate(0, 0, -2));

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_UBO_TEST::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        Function<Integer, Matrix4f> base = (j) -> new Matrix4f()
            .translate(j - 1.5f, 0.75f, 0)
            .rotateY((float) MathUtil.animateRadians(4d))
            .rotateZ((float) Math.toRadians(180)) // Todo: flip the view probably ?
            .scale(0.05f);

        var sbo = SBO.BONES.create((Object) new Matrix4f[] {
            new Matrix4f(base.apply(0)).translate(0, -10f, 0),
            new Matrix4f(base.apply(1)),
            new Matrix4f(base.apply(2)).translate(0, 10f, 0),
            new Matrix4f(base.apply(3)).rotateZ((float) (Math.PI * 0.25f))
        });

        var smallSbo = SBO.BONES.create((Object) new Matrix4f[] {
            new Matrix4f(base.apply(1)).scale(0.5f)
        });

        flightFrame.sboBuffer.writeToBuffer(SBO.BONES::memcpy, sbo);
        flightFrame.sboBuffer.flush();

//        flightFrame.sboBuffer.writeToBuffer(SBO.BONES::memcpy, List.of(smallSbo), 64, 64);
//        flightFrame.sboBuffer.flush(64, 64);

        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        model3d.bind(frameInfo.commandBuffer);
        model3d.draw(frameInfo.commandBuffer, 4);
    }

    @Override
    public void cleanup()
    {
        model3d.destroy();
        globalSetLayout.cleanup();
        globalPool.cleanup();

        for (FlightFrame flightFrame : frames)
        {
            flightFrame.uboBuffer.cleanup();
            flightFrame.sboBuffer.cleanup();
        }
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer sboBuffer;
        long descriptorSet;
    }
}
