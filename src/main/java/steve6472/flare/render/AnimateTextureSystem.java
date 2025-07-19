package steve6472.flare.render;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
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
import steve6472.flare.framebuffer.AnimatedAtlasFrameBuffer;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.impl.UIRenderImpl;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.struct.def.Vertex;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.test.TestKeybinds;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AnimateTextureSystem extends RenderSystem
{
    private static final Logger LOGGER = Log.getLogger(AnimateTextureSystem.class);
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    private final List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;
    public final SpriteAtlas atlas;

    public AnimateTextureSystem(MasterRenderer masterRenderer, Atlas atlas)
    {
        super(masterRenderer, Pipelines.UI_TEXTURE);
        if (!(atlas instanceof SpriteAtlas spriteAtlas))
            throw new RuntimeException("Passed atlas '%s' is not a Sprite Atlas".formatted(atlas.key()));
        this.atlas = spriteAtlas;
        AnimationAtlas animationAtlas = spriteAtlas.getAnimationAtlas();
        Objects.requireNonNull(animationAtlas, "Atlas '%s' does not contain animations".formatted(atlas.key()));

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
                    .writeImage(1, stack, animationAtlas.getSampler())
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

    List<Struct> structList;

    protected final void vertex(Vector3f position, Vector3f tint, Vector3f data)
    {
        structList.add(Vertex.POS3F_COL3F_DATA3F.create(position, tint, data));
    }



    protected final SpriteEntry getTextureEntry(Key textureKey)
    {
        SpriteEntry uiTextureEntry = atlas.getAnimationAtlas().getSprite(textureKey);
        if (uiTextureEntry == null)
        {
            Log.warningOnce(LOGGER, "Missing UI Texture for " + textureKey);
        }
        return uiTextureEntry;
    }

    protected final void sprite(int x, int y, float zIndex, int width, int height, int pixelWidth, int pixelHeight, Vector3f tint, @NotNull Key textureKey)
    {
        createSprite(x, y, zIndex, width, height, pixelWidth, pixelHeight, tint, getTextureEntry(textureKey));
    }

    protected static final Vector3f NO_TINT = new Vector3f(1.0f);

    protected final void sprite(int x, int y, float zIndex, int width, int height, int pixelWidth, int pixelHeight, @NotNull Key textureKey)
    {
        sprite(x, y, zIndex, width, height, pixelWidth, pixelHeight, NO_TINT, textureKey);
    }

    protected final void createSprite(
        int x, int y, float zIndex,
        int width, int height,
        int pixelWidth, int pixelHeight,
        Vector3f tint,
        SpriteEntry texture)
    {
        int index;
        if (texture == null)
        {
            index = 0;
        } else
        {
            index = texture.index();
        }

        // Fit zIndex to 0 - 0.1 range
        zIndex /= 256f;
        zIndex /= 10f;

        // Define base vertices
        Vector3f vtl = new Vector3f(x, y , zIndex);
        Vector3f vbl = new Vector3f(x, y + height, zIndex);
        Vector3f vbr = new Vector3f(x + width, y + height, zIndex);
        Vector3f vtr = new Vector3f(x + width, y, zIndex);
        //noinspection SuspiciousNameCombination
        Vector3f vertexData = new Vector3f(index, pixelWidth, pixelHeight);

        vertex(vtl, tint, vertexData);
        vertex(vbl, tint, vertexData);
        vertex(vbr, tint, vertexData);

        vertex(vbr, tint, vertexData);
        vertex(vtr, tint, vertexData);
        vertex(vtl, tint, vertexData);
    }

    int w = 8;
    int h = 0;

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        structList = new ArrayList<>();

        if (TestKeybinds.TO_RIGHT.isActive())
            w++;
        if (TestKeybinds.TO_LEFT.isActive())
            w--;

        if (TestKeybinds.TO_DOWN.isActive())
            h++;
        if (TestKeybinds.TO_UP.isActive())
            h--;


        int scale = 16;
        sprite(0, 0, 0, 8 * scale, 8 * scale, w, h, Key.withNamespace("test", "box_animated"));

        if (structList.isEmpty())
            return;

        FlightFrame flightFrame = frames.get(frameInfo.frameIndex());

        Camera camera = new Camera();
        int windowWidth = getMasterRenderer().getWindow().getWidth();
        int windowHeight = getMasterRenderer().getWindow().getHeight();

        camera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, 1f);
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

        buffer.writeToBuffer(vertex()::memcpy, structList);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), structList.size(), 1, 0, 0);
    }

    private Struct updateUITextures()
    {
        Struct[] textureSettings;
        Atlas atlas = FlareRegistries.ATLAS.get(FlareConstants.ATLAS_UI);
        if (atlas instanceof SpriteAtlas spriteAtlas && spriteAtlas.getAnimationAtlas() != null)
        {
            textureSettings = spriteAtlas.getAnimationAtlas().createTextureSettings();
        } else
        {
            textureSettings = atlas.createTextureSettings();
        }
        return SBO.SPRITE_ENTRIES.create((Object) textureSettings);
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