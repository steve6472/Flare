package steve6472.flare.render;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.flare.Camera;
import steve6472.flare.FlareConstants;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.atlas.AnimationAtlas;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.SpriteAtlas;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.descriptors.DescriptorPool;
import steve6472.flare.descriptors.DescriptorSetLayout;
import steve6472.flare.descriptors.DescriptorWriter;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.struct.def.Vertex;
import steve6472.flare.ui.textures.SpriteEntry;

import java.nio.LongBuffer;
import java.util.ArrayList;
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
    private final List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;
    public final SpriteAtlas atlas;
    public final AnimationTicker ticker;

    public AnimateTextureSystem(MasterRenderer masterRenderer, Atlas atlas)
    {
        super(masterRenderer, Pipelines.ATLAS_ANIMATION);
        if (!(atlas instanceof SpriteAtlas spriteAtlas))
            throw new RuntimeException("Passed atlas '%s' is not a Sprite Atlas".formatted(atlas.key()));
        this.atlas = spriteAtlas;
        AnimationAtlas animationAtlas = spriteAtlas.getAnimationAtlas();
        Objects.requireNonNull(animationAtlas, "Atlas '%s' does not contain animations".formatted(atlas.key()));

        ticker = new AnimationTicker(animationAtlas);

        globalSetLayout = DescriptorSetLayout.builder(device)
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
            FlightFrame frame = new FlightFrame();
            frames.add(frame);

            VkBuffer global = new VkBuffer(
                device,
                UBO.GLOBAL_CAMERA_UBO.sizeof(),
                1,
                VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            global.map();
            frame.uboBuffer = global;

            VkBuffer animationSettings = new VkBuffer(
                device,
                SBO.ANIMATION_ENTRIES.sizeof(),
                1,
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            animationSettings.map();
            frame.sboAnimDataSettings = animationSettings;

            updateSbo(frame.sboAnimDataSettings);

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);

                frame.descriptorSet = descriptorWriter
                    .writeBuffer(0, stack, frame.uboBuffer, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .writeImage(1, stack, animationAtlas.getSampler())
                    .writeBuffer(2, stack, frame.sboAnimDataSettings)
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
        List<Struct> structList = new ArrayList<>();

        atlas.getAnimationAtlas().getSprites().forEach((key, entry) -> {
            if (key.equals(FlareConstants.ERROR_TEXTURE))
                return;

            SpriteEntry sprite = atlas.getSprite(key);
            int x = (int) (sprite.uv().x * atlas.frameBuffer.width);
            int y = (int) (sprite.uv().y * atlas.frameBuffer.height);
            int u = (int) (sprite.uv().z * atlas.frameBuffer.width);
            int v = (int) (sprite.uv().w * atlas.frameBuffer.height);

            Vector3f vtl = new Vector3f(x, y, 0);
            Vector3f vbl = new Vector3f(x, v, 0);
            Vector3f vbr = new Vector3f(u, v, 0);
            Vector3f vtr = new Vector3f(u, y, 0);
            Vector3f vertexData = new Vector3f(entry.index(), entry.data().animation().orElseThrow().width(), entry.data().animation().orElseThrow().height());

            structList.add(Vertex.POS3F_DATA3F.create(vtl, vertexData));
            structList.add(Vertex.POS3F_DATA3F.create(vbl, vertexData));
            structList.add(Vertex.POS3F_DATA3F.create(vbr, vertexData));

            structList.add(Vertex.POS3F_DATA3F.create(vbr, vertexData));
            structList.add(Vertex.POS3F_DATA3F.create(vtr, vertexData));
            structList.add(Vertex.POS3F_DATA3F.create(vtl, vertexData));
        });

        if (structList.isEmpty())
            return;

        FlightFrame flightFrame = frames.get(frameInfo.frameIndex());

        Camera camera = new Camera();
        int windowWidth = atlas.frameBuffer.width;
        int windowHeight = atlas.frameBuffer.height;

        camera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, 1f);
        camera.setViewYXZ(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(camera.getProjectionMatrix(), camera.getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.uboBuffer.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * frameInfo.camera().cameraIndex);
        flightFrame.uboBuffer.flush(singleInstanceSize, (long) singleInstanceSize * frameInfo.camera().cameraIndex);

        pipeline().bind(frameInfo.commandBuffer());

        updateSbo(flightFrame.sboAnimDataSettings);

        vkCmdBindDescriptorSets(
            frameInfo.commandBuffer(),
            VK_PIPELINE_BIND_POINT_GRAPHICS,
            pipeline().pipelineLayout(),
            0,
            stack.longs(flightFrame.descriptorSet),
            stack.ints(singleInstanceSize * frameInfo.camera().cameraIndex));

        buffer.writeToBuffer(vertex()::memcpy, structList);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), structList.size(), 1, 0, 0);
    }

    private void updateSbo(VkBuffer sboBuffer)
    {
        long now = System.currentTimeMillis();
        Struct sbo = ticker.createSbo(now);
        sboBuffer.writeToBuffer(SBO.ANIMATION_ENTRIES::memcpy, sbo);
        ticker.tick(now);
    }

    @Override
    public void cleanup()
    {
        globalSetLayout.cleanup();
        globalPool.cleanup();

        if (buffer != null)
            buffer.cleanup();

        for (FlightFrame flightFrame : frames)
        {
            flightFrame.uboBuffer.cleanup();
            flightFrame.sboAnimDataSettings.cleanup();
        }
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer sboAnimDataSettings;
        long descriptorSet;
    }
}