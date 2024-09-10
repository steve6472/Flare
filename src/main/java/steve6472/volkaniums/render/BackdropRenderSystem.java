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
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.model.LoadedModel;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class BackdropRenderSystem extends RenderSystem
{
    Model3d model3d;

    private DescriptorPool globalPool;
    private DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    Texture texture;
    TextureSampler sampler;

    public BackdropRenderSystem(VkDevice device, Pipeline pipeline, Commands commands, VkQueue graphicsQueue)
    {
        super(device, pipeline);

        createModel(commands, graphicsQueue);

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

        texture = new Texture();
        texture.createTextureImage(device, "resources\\backdrop.png", commands.commandPool, graphicsQueue);
        sampler = new TextureSampler(texture, device);

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            frame.add(new FlightFrame());

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.get(i).uboBuffer = global;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.get(i).descriptorSet = descriptorWriter
                    .writeBuffer(0, frame.get(i).uboBuffer, stack)
                    .writeImage(1, sampler, stack)
                    .build();
            }
        }
    }

    private void createModel(Commands commands, VkQueue graphicsQueue)
    {
        final String PATH = "resources\\backdrop.bbmodel";
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
        model3d.createVertexBuffer(device, commands, graphicsQueue, decode.getOrThrow().getFirst().toPrimitiveModel().toVkVertices(1f / 16f), Vertex.POS3F_COL3F_UV);
    }

    @Override
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        FlightFrame flightFrame = frame.get(frameInfo.frameIndex);
        // Update

        var globalUBO = UBO.GLOBAL_UBO.create(frameInfo.camera.getProjectionMatrix(), frameInfo.camera.getViewMatrix(), new Matrix4f[] {
            new Matrix4f().translate(0, 0, 0).rotateY(0)
        });

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_UBO::memcpy, globalUBO);
        flightFrame.uboBuffer.flush();

        pipeline.bind(frameInfo.commandBuffer);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer,
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline.pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            null);

        Struct push = Push.PUSH.create(new Matrix4f(),
            new Vector4f(1, 1, 1, 1.0f),
            0);

        Push.PUSH.push(push, frameInfo.commandBuffer, pipeline.pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0);

        model3d.bind(frameInfo.commandBuffer);
        model3d.draw(frameInfo.commandBuffer);
    }

    @Override
    public void cleanup()
    {
        sampler.cleanup(device);
        texture.cleanup(device);
        model3d.destroy();
        globalSetLayout.cleanup();
        globalPool.cleanup();

        for (FlightFrame flightFrame : frame)
            flightFrame.uboBuffer.cleanup();
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        long descriptorSet;
    }
}
