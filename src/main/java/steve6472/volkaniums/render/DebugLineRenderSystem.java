package steve6472.volkaniums.render;

import org.lwjgl.system.MemoryStack;
import steve6472.volkaniums.*;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.render.debug.DebugRender;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public class DebugLineRenderSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);

    private VkBuffer buffer;

    public DebugLineRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline);

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
                    .writeBuffer(0, frame.get(i).uboBuffer, stack, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .build();
            }
        }

        if (VisualSettings.DEBUG_LINE_SINGLE_BUFFER.get())
        {
            buffer = new VkBuffer(
                masterRenderer.getDevice(),
                Vertex.POS3F_COL4F.sizeof(),
                262144,
                VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
            );
            buffer.map();
        }
    }

    @Override
    public long[] setLayouts()
    {
        return new long[] {globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        if (VisualSettings.DEBUG_LINE_SINGLE_BUFFER.get())
            renderSingleBuffer(frameInfo, stack);
        else
            renderRotatingBuffer(frameInfo, stack);
    }

    private void renderSingleBuffer(FrameInfo frameInfo, MemoryStack stack)
    {
        List<Struct> verticies = DebugRender.getInstance().createVerticies();
        if (verticies.isEmpty())
            return;

        FlightFrame flightFrame = frame.get(frameInfo.frameIndex());

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(frameInfo.camera().getProjectionMatrix(), frameInfo.camera().getViewMatrix());
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

        buffer.writeToBuffer(Vertex.POS3F_COL4F::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);
    }

    public void renderRotatingBuffer(FrameInfo frameInfo, MemoryStack stack)
    {
        List<Struct> verticies = DebugRender.getInstance().createVerticies();
        if (verticies.isEmpty())
            return;

        FlightFrame flightFrame = frame.get(frameInfo.frameIndex());

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(frameInfo.camera().getProjectionMatrix(), frameInfo.camera().getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * frameInfo.camera().cameraIndex);
        flightFrame.uboBuffer.flush(singleInstanceSize, (long) singleInstanceSize * frameInfo.camera().cameraIndex);

        if (flightFrame.vertexBuffer != null)
        {
            flightFrame.vertexBuffer.cleanup();
        }

        pipeline().bind(frameInfo.commandBuffer());

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            stack.ints(singleInstanceSize * frameInfo.camera().cameraIndex));

        VkBuffer stagingBuffer = new VkBuffer(
            device,
            Vertex.POS3F_COL4F.sizeof(),
            verticies.size(),
            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        stagingBuffer.map();
        stagingBuffer.writeToBuffer(Vertex.POS3F_COL4F::memcpy, verticies);

        VkBuffer vertexBuffer = new VkBuffer(
            device,
            Vertex.POS3F_COL4F.sizeof(),
            verticies.size(),
            VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_HEAP_DEVICE_LOCAL_BIT);

        long bufferSize = (long) Vertex.POS3F_COL4F.sizeof() * verticies.size();
        VkBuffer.copyBuffer(getMasterRenderer().getCommands(), device, getMasterRenderer().getGraphicsQueue(), stagingBuffer, vertexBuffer, bufferSize);
        stagingBuffer.cleanup();

        LongBuffer vertexBuffers = stack.longs(vertexBuffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);

        flightFrame.vertexBuffer = vertexBuffer;
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
