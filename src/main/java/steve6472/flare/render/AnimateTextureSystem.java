package steve6472.flare.render;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Key;
import steve6472.flare.Camera;
import steve6472.flare.FlareConstants;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.descriptors.DescriptorPool;
import steve6472.flare.descriptors.DescriptorSetLayout;
import steve6472.flare.descriptors.DescriptorWriter;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.impl.UIRenderImpl;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.ui.textures.SpriteEntry;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AnimateTextureSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    private final List<UIRenderSystem.FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;

    private final UIRenderImpl renderImpl;
    private final float far;

    public AnimateTextureSystem(MasterRenderer masterRenderer, @NonNull UIRenderImpl renderImpl, float far)
    {
        super(masterRenderer, Pipelines.UI_TEXTURE);
        Objects.requireNonNull(renderImpl);
        this.renderImpl = renderImpl;
        this.far = far;

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .addBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
            .build();

        for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
        {
            UIRenderSystem.FlightFrame frame = new UIRenderSystem.FlightFrame();
            frames.add(frame);

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_CAMERA_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.uboBuffer = global;

            VkBuffer textureSettings = new VkBuffer(
                device,
                SBO.SPRITE_ENTRIES.sizeof(),
                1,
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            textureSettings.map();
            frame.sboTextureSettings = textureSettings;

            frame.sboTextureSettings.writeToBuffer(SBO.SPRITE_ENTRIES::memcpy, updateUITextures());

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.descriptorSet = descriptorWriter
                    .writeBuffer(0, stack, frame.uboBuffer, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .writeImage(1, stack, FlareRegistries.SAMPLER.get(FlareConstants.UI_TEXTURE))
                    .writeBuffer(2, stack, frame.sboTextureSettings)
                    .build();
            }
        }

        buffer = new VkBuffer(
            masterRenderer.getDevice(),
            vertex().sizeof(),
            32768 * 4, // max 32k sprites at once, should be enough....
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

        UIRenderSystem.FlightFrame flightFrame = frames.get(frameInfo.frameIndex());

        Camera camera = new Camera();
        int windowWidth = getMasterRenderer().getWindow().getWidth();
        int windowHeight = getMasterRenderer().getWindow().getHeight();

        camera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, far);
        camera.setViewYXZ(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));

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


    private Struct updateUITextures()
    {
        Collection<Key> keys = FlareRegistries.SPRITE.keys();
        Struct[] textureSettings = new Struct[keys.size()];
        keys.forEach(key ->
        {
            SpriteEntry uiTextureEntry = FlareRegistries.SPRITE.get(key);
            textureSettings[uiTextureEntry.index()] = uiTextureEntry.toStruct();
        });

        return SBO.SPRITE_ENTRIES.create((Object) textureSettings);
    }

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        if (buffer != null)
            buffer.cleanup();

        for (UIRenderSystem.FlightFrame flightFrame : frames)
        {
            flightFrame.uboBuffer.cleanup();
            flightFrame.sboTextureSettings.cleanup();
        }
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer sboTextureSettings;
        long descriptorSet;
    }
}