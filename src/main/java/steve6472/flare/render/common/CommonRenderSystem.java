package steve6472.flare.render.common;

import org.lwjgl.system.MemoryStack;
import steve6472.flare.Camera;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.descriptors.DescriptorPool;
import steve6472.flare.descriptors.DescriptorSetLayout;
import steve6472.flare.descriptors.DescriptorWriter;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.render.Reloadable;
import steve6472.flare.render.RenderSystem;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.UBO;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 9/6/2025
 * Project: Flare <br>
 */
public abstract class CommonRenderSystem extends RenderSystem implements Reloadable
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    private final List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    // Used for updating the texture sampler
    private final CommonBuilder builder;

    public CommonRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline, CommonBuilder commonBuilder)
    {
        super(masterRenderer, pipeline);
        this.builder = commonBuilder;

        globalPool = createGlobalPool(commonBuilder);
        globalSetLayout = createGlobalSetLayout(commonBuilder);

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            FlightFrame flightFrame = createFlightFrame(commonBuilder);
            frames.add(flightFrame);

            if (commonBuilder.postCreation != null)
                commonBuilder.postCreation.accept(flightFrame);
        }
    }

    @Override
    public void reload()
    {
        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            FlightFrame frame = frames.get(i);
            onReload(frame);
        }
    }

    protected void onReload(FlightFrame frame)
    {
        cleanupFlightFrameUserObjects(frame);

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
            descriptorWriter.writeBuffer(0, stack, frame.cameraUbo, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT);

            int j = 1;
            for (CommonEntry entry : builder.entries)
            {
                frame.userObjects[j - 1] = entry.createObject(device);
                entry.write(descriptorWriter, j, stack, frame.userObjects[j - 1]);
                j++;
            }

            descriptorWriter.override(frame.descriptorSet, stack);
        }
    }

    protected DescriptorPool createGlobalPool(CommonBuilder commonBuilder)
    {
        DescriptorPool.Builder builder = DescriptorPool
            .builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, MAX_FRAMES_IN_FLIGHT);

        for (CommonEntry entry : commonBuilder.entries)
        {
            int type = entry.type();
            if (type == 0) continue;
            builder.addPoolSize(type, MAX_FRAMES_IN_FLIGHT);
        }

        return builder.build();
    }

    protected DescriptorSetLayout createGlobalSetLayout(CommonBuilder commonBuilder)
    {
        DescriptorSetLayout.Builder builder = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT);

        int i = 1;
        for (CommonEntry entry : commonBuilder.entries)
        {
            int type = entry.type();
            if (type == 0) continue;
            builder.addBinding(i, type, entry.stage(), entry.count());
            i++;
        }

        return builder.build();
    }

    protected FlightFrame createFlightFrame(CommonBuilder commonBuilder)
    {
        FlightFrame frame = new FlightFrame();

        VkBuffer global = new VkBuffer(device, UBO.GLOBAL_CAMERA_UBO.sizeof(), 1, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
        global.map();
        frame.cameraUbo = global;

        frame.userObjects = new Object[commonBuilder.entries.size()];

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
            descriptorWriter.writeBuffer(0, stack, frame.cameraUbo, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT);

            int i = 1;
            for (CommonEntry entry : commonBuilder.entries)
            {
                frame.userObjects[i - 1] = entry.createObject(device);
                entry.write(descriptorWriter, i, stack, frame.userObjects[i - 1]);
                i++;
            }

            frame.descriptorSet = descriptorWriter.build();
        }

        return frame;
    }

    @Override
    public long[] setLayouts()
    {
        return new long[]{globalSetLayout.descriptorSetLayout};
    }

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        if (!shouldRender())
            return;

        FlightFrame flightFrame = frames.get(frameInfo.frameIndex());

        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;
        setupCameraUbo(flightFrame, frameInfo.camera());
        pipeline().bind(frameInfo.commandBuffer());

        updateData(flightFrame, frameInfo);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            stack.ints(singleInstanceSize * frameInfo.camera().cameraIndex));

        render(flightFrame, frameInfo, stack);
    }

    protected abstract void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack);
    protected abstract void updateData(FlightFrame flightFrame, FrameInfo frameInfo);

    protected boolean shouldRender()
    {
        return true;
    }

    protected void setupCameraUbo(FlightFrame flightFrame, Camera camera)
    {
        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(camera.getProjectionMatrix(), camera.getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.cameraUbo.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * camera.cameraIndex);
        flightFrame.cameraUbo.flush(singleInstanceSize, (long) singleInstanceSize * camera.cameraIndex);
    }

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        for (FlightFrame flightFrame : frames)
        {
            flightFrame.cameraUbo.cleanup();
            cleanupFlightFrameUserObjects(flightFrame);
        }
    }

    private void cleanupFlightFrameUserObjects(FlightFrame flightFrame)
    {
        for (Object userObject : flightFrame.userObjects)
        {
            if (userObject instanceof VkBuffer buffer)
            {
                buffer.cleanup();
            } else
            {
                if (!(userObject instanceof TextureSampler) && !(userObject instanceof TextureSampler[]))
                {
                    throw new RuntimeException("Uncleanable type " + userObject.getClass().getSimpleName());
                }
            }
        }
    }
}
