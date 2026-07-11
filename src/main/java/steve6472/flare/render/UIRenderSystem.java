package steve6472.flare.render;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.flare.Camera;
import steve6472.flare.FlareConstants;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.render.common.CommonBuilder;
import steve6472.flare.render.common.CommonRenderSystem;
import steve6472.flare.render.common.FlightFrame;
import steve6472.flare.render.impl.UIRenderImpl;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;

import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class UIRenderSystem extends CommonRenderSystem
{
    private static final int SPRITE_ENTRIES_INDEX = 1;

    private final UIRenderImpl renderImpl;
    private final float far;
    private final VkBuffer buffer;

    public UIRenderSystem(MasterRenderer masterRenderer, @NonNull UIRenderImpl renderImpl, float far)
    {
        super(masterRenderer,
            Pipelines.UI_TEXTURE,
            CommonBuilder.create()
                .entryImage(BuiltInFlareRegistries.ATLAS.get(FlareConstants.ATLAS_UI).orElseThrow().value().getSampler())
                .entrySBO(SBO.SPRITE_ENTRIES.sizeof(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_SHADER_STAGE_FRAGMENT_BIT)
                .postCreation(ff -> ff.getBuffer(SPRITE_ENTRIES_INDEX).writeToBuffer(SBO.SPRITE_ENTRIES::memcpy, updateUITextures())));

        Objects.requireNonNull(renderImpl);
        this.renderImpl = renderImpl;
        this.far = far;

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
    protected void onReload(FlightFrame frame)
    {
        super.onReload(frame);
        frame.getBuffer(SPRITE_ENTRIES_INDEX).writeToBuffer(SBO.SPRITE_ENTRIES::memcpy, updateUITextures());
    }

    @Override
    public void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack)
    {
        List<Struct> verticies = new ArrayList<>();
        renderImpl.setStructList(verticies);
        renderImpl.render();

        if (verticies.isEmpty())
            return;

        buffer.writeToBuffer(vertex()::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);
    }

    @Override
    protected void setupCameraUbo(FlightFrame flightFrame, Camera camera)
    {
        Camera orthoCamera = new Camera();
        int windowWidth = getMasterRenderer().getWindow().getWidth();
        int windowHeight = getMasterRenderer().getWindow().getHeight();

        orthoCamera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, far);
        orthoCamera.setViewYXZ(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(orthoCamera.getProjectionMatrix(), orthoCamera.getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.cameraUbo.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * camera.cameraIndex);
        flightFrame.cameraUbo.flush(singleInstanceSize, (long) singleInstanceSize * camera.cameraIndex);
    }

    @Override
    protected void updateData(FlightFrame flightFrame, FrameInfo frameInfo)
    {
    }

    private static Struct updateUITextures()
    {
        Struct[] textureSettings = BuiltInFlareRegistries.ATLAS.get(FlareConstants.ATLAS_UI).orElseThrow().value().createTextureSettings();
        return SBO.SPRITE_ENTRIES.create((Object) textureSettings);
    }

    @Override
    public void cleanup()
    {
        super.cleanup();

        if (buffer != null)
            buffer.cleanup();
    }
}
