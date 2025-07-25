package steve6472.flare.render;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.flare.Camera;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.descriptors.DescriptorPool;
import steve6472.flare.descriptors.DescriptorSetLayout;
import steve6472.flare.descriptors.DescriptorWriter;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.render.impl.UILineRenderImpl;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.UBO;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 12/19/2024
 * Project: MoonDust <br>
 */
public class UILineRender extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);

    private final UILineRenderImpl renderImpl;
    private final VkBuffer buffer;

    public UILineRender(MasterRenderer masterRenderer, UILineRenderImpl renderImpl)
    {
        super(masterRenderer, Pipelines.UI_DEBUG_LINE);
        this.renderImpl = renderImpl;

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            frame.add(new FlightFrame());

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_CAMERA_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.get(i).uboBuffer = global;

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.get(i).descriptorSet = descriptorWriter
                    .writeBuffer(0, stack, frame.get(i).uboBuffer, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .build();
            }
        }

        buffer = new VkBuffer(
            masterRenderer.getDevice(),
            vertex().sizeof(),
            262144,
            VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
        );
        buffer.map();
    }

    @Override
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        List<Struct> verticies = new ArrayList<>();

        renderImpl.setStructList(verticies);
        renderImpl.render();

        if (verticies.isEmpty())
            return;

        FlightFrame flightFrame = frame.get(frameInfo.frameIndex());

        Camera camera = new Camera();
        int windowWidth = this.getMasterRenderer().getWindow().getWidth();
        int windowHeight = this.getMasterRenderer().getWindow().getHeight();
        camera.setOrthographicProjection(0.0F, (float)windowWidth, 0.0F, (float)windowHeight, 0.0F, renderImpl.getFar());
        camera.setViewYXZ(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F));
        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(camera.getProjectionMatrix(), camera.getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * frameInfo.camera().cameraIndex);
        flightFrame.uboBuffer.flush(singleInstanceSize, (long) singleInstanceSize * frameInfo.camera().cameraIndex);

        pipeline().bind(frameInfo.commandBuffer());

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            stack.ints(singleInstanceSize * frameInfo.camera().cameraIndex));

        buffer.writeToBuffer(vertex()::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);
    }

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        if (buffer != null)
            buffer.cleanup();

        for (FlightFrame flightFrame : frame)
        {
            flightFrame.uboBuffer.cleanup();
            if (flightFrame.vertexBuffer != null)
            {
                flightFrame.vertexBuffer.cleanup();
            }
        }
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer vertexBuffer;
        long descriptorSet;
    }
}
