package steve6472.flare.render;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.flare.Camera;
import steve6472.flare.FlareConstants;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.AnimationAtlas;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.SpriteAtlas;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.render.common.CommonBuilder;
import steve6472.flare.render.common.CommonRenderSystem;
import steve6472.flare.render.common.FlightFrame;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.ui.textures.animation.SpriteAnimation;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class AnimateTextureSystem extends CommonRenderSystem
{
    private final VkBuffer buffer;
    public final SpriteAtlas atlas;
    public final AnimationTicker ticker;

    private static TextureSampler atlasSampler(Atlas atlas)
    {
        if (!(atlas instanceof SpriteAtlas spriteAtlas))
            throw new RuntimeException("Passed atlas '%s' is not a Sprite Atlas".formatted(atlas.key()));
        return spriteAtlas.getAnimationAtlas().getSampler();
    }

    public AnimateTextureSystem(MasterRenderer masterRenderer, Atlas atlas)
    {
        super(masterRenderer,
            Pipelines.ATLAS_ANIMATION,
            CommonBuilder.create()
                .entryImage(atlasSampler(atlas))
                .entrySBO(UBO.GLOBAL_CAMERA_UBO.sizeof(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_SHADER_STAGE_FRAGMENT_BIT));

        if (!(atlas instanceof SpriteAtlas spriteAtlas))
            throw new RuntimeException("Passed atlas '%s' is not a Sprite Atlas".formatted(atlas.key()));
        this.atlas = spriteAtlas;
        AnimationAtlas animationAtlas = spriteAtlas.getAnimationAtlas();
        Objects.requireNonNull(animationAtlas, "Atlas '%s' does not contain animations".formatted(atlas.key()));

        ticker = new AnimationTicker(animationAtlas);

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
    protected void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack)
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
            SpriteAnimation animation = entry.data().animation().orElseThrow();
            Vector3f vertexData = new Vector3f(entry.index(), animation.width(), animation.height());

            structList.add(vertex().create(vtl, vertexData));
            structList.add(vertex().create(vbl, vertexData));
            structList.add(vertex().create(vbr, vertexData));

            structList.add(vertex().create(vbr, vertexData));
            structList.add(vertex().create(vtr, vertexData));
            structList.add(vertex().create(vtl, vertexData));
        });

        if (structList.isEmpty())
            return;

        buffer.writeToBuffer(vertex()::memcpy, structList);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), structList.size(), 1, 0, 0);
    }

    @Override
    protected void updateData(FlightFrame flightFrame, FrameInfo frameInfo)
    {
        long now = System.currentTimeMillis();
        VkBuffer sboBuffer = flightFrame.getBuffer(1);
        Struct sbo = ticker.createSbo(now);
        sboBuffer.writeToBuffer(SBO.ANIMATION_ENTRIES::memcpy, sbo);
        ticker.tick(now);
    }

    @Override
    protected void setupCameraUbo(FlightFrame flightFrame, Camera camera)
    {
        Camera orthoCamera = new Camera();
        int windowWidth = atlas.frameBuffer.width;
        int windowHeight = atlas.frameBuffer.height;

        orthoCamera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, 1f);
        orthoCamera.setViewYXZ(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(orthoCamera.getProjectionMatrix(), orthoCamera.getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.cameraUbo.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * camera.cameraIndex);
        flightFrame.cameraUbo.flush(singleInstanceSize, (long) singleInstanceSize * camera.cameraIndex);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        if (buffer != null)
            buffer.cleanup();
    }
}