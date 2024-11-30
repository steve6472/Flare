package steve6472.flare.render;

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
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Push;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.struct.def.Vertex;
import steve6472.flare.ui.textures.UITextureEntry;
import steve6472.flare.ui.textures.UITextureLoader;
import steve6472.test.TestKeybinds;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class UIRenderSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    private final List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;

    private static final Vector3f NO_TINT = new Vector3f(1.0f);

    public UIRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline);

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
                SBO.UI_TEXTURE_ENTRIES.sizeof(),
                1,
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            textureSettings.map();
            frame.sboTextureSettings = textureSettings;

            frame.sboTextureSettings.writeToBuffer(SBO.UI_TEXTURE_ENTRIES::memcpy, updateUITextures());

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
        List<Struct> verticies = createVerticies();

        if (verticies.isEmpty())
            return;

        FlightFrame flightFrame = frames.get(frameInfo.frameIndex());

        Camera camera = new Camera();
        int windowWidth = getMasterRenderer().getWindow().getWidth();
        int windowHeight = getMasterRenderer().getWindow().getHeight();

        camera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, 256f);
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

//        Struct windowSize = Push.WINDOW_SIZE.create((float) getMasterRenderer().getWindow().getWidth(), (float) getMasterRenderer().getWindow().getHeight());
        Struct windowSize = Push.WINDOW_SIZE.create((float) UITextureLoader.getAtlasWidth(), (float) UITextureLoader.getAtlasWidth());
        Push.WINDOW_SIZE.push(windowSize, frameInfo.commandBuffer(), pipeline().pipelineLayout(), VK_SHADER_STAGE_FRAGMENT_BIT, 0);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);

        if (TestKeybinds.TO_UP.isActive()) pixelH -= 1;
        if (TestKeybinds.TO_DOWN.isActive()) pixelH += 1;
        if (TestKeybinds.TO_LEFT.isActive()) pixelW -= 1;
        if (TestKeybinds.TO_RIGHT.isActive()) pixelW += 1;
    }

    int pixelW = 20;
    int pixelH = 20;


    private List<Struct> createVerticies()
    {
        List<Struct> structs = new ArrayList<>();
        UITextureEntry uiTextureEntry = FlareRegistries.UI_TEXTURE.get(Key.withNamespace("test", "box"));
        createSprite(structs, 0, 0, 0, pixelW * 10, pixelH * 10, uiTextureEntry);
        return structs;
    }

    private void createSprite(List<Struct> structs, int x, int y, float zIndex, int width, int height, UITextureEntry texture)
    {
        createSprite(structs, x, y, zIndex, width, height, texture, NO_TINT);
    }

    private void createSprite(List<Struct> structs, int x, int y, float zIndex, int width, int height, UITextureEntry texture, Vector3f tint)
    {
        int index = texture.index();

        // Define base vertices
        Vector3f vtl = new Vector3f(x, y , zIndex);
        Vector3f vbl = new Vector3f(x, y + height, zIndex);
        Vector3f vbr = new Vector3f(x + width, y + height, zIndex);
        Vector3f vtr = new Vector3f(x + width, y, zIndex);
        Vector3f vertexData = new Vector3f(index, pixelW, pixelH);

        structs.add(vertex().create(vtl, tint, vertexData));
        structs.add(vertex().create(vbl, tint, vertexData));
        structs.add(vertex().create(vbr, tint, vertexData));

        structs.add(vertex().create(vbr, tint, vertexData));
        structs.add(vertex().create(vtr, tint, vertexData));
        structs.add(vertex().create(vtl, tint, vertexData));
    }

    private Struct updateUITextures()
    {
        Collection<Key> keys = FlareRegistries.UI_TEXTURE.keys();
        Struct[] textureSettings = new Struct[keys.size()];
        keys.forEach(key ->
        {
            UITextureEntry uiTextureEntry = FlareRegistries.UI_TEXTURE.get(key);
            textureSettings[uiTextureEntry.index()] = uiTextureEntry.toStruct();
        });

        return SBO.UI_TEXTURE_ENTRIES.create((Object) textureSettings);
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
