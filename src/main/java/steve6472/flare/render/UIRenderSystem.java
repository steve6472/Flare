package steve6472.flare.render;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Key;
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
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public final class UIRenderSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    private final List<FlightFrame> frames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;

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
            Vertex.POS3F_UV_DATA3F.sizeof(),
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

        buffer.writeToBuffer(Vertex.POS3F_UV_DATA3F::memcpy, verticies);

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

    float pixelW = 20;
    float pixelH = 20;

    private List<Struct> createVerticies()
    {
        List<Struct> structs = new ArrayList<>();
        UITextureEntry uiTextureEntry = FlareRegistries.UI_TEXTURE.get(Key.withNamespace("test", "box"));

        float x = 0;
        float y = 0;

        float w = pixelW / uiTextureEntry.pixelSize().x;
        float h = pixelH / uiTextureEntry.pixelSize().y;

        Vector4f uv = uiTextureEntry.uv();
        int index = uiTextureEntry.index();

        Vector2f tl = new Vector2f(0, 0);
        Vector2f br = new Vector2f(1, 1);

        Vector3f vtl = new Vector3f(x - w / 2f, y + h / 2f, 0);
        Vector3f vbl = new Vector3f(x - w / 2f, y - h / 2f, 0);
        Vector3f vbr = new Vector3f(x + w / 2f, y - h / 2f, 0);
        Vector3f vtr = new Vector3f(x + w / 2f, y + h / 2f, 0);

        structs.add(Vertex.POS3F_UV_DATA3F.create(vtl, new Vector2f(tl.x, tl.y), new Vector3f(index, pixelW, pixelH)));
        structs.add(Vertex.POS3F_UV_DATA3F.create(vbl, new Vector2f(tl.x, br.y), new Vector3f(index, pixelW, pixelH)));
        structs.add(Vertex.POS3F_UV_DATA3F.create(vbr, new Vector2f(br.x, br.y), new Vector3f(index, pixelW, pixelH)));

        structs.add(Vertex.POS3F_UV_DATA3F.create(vbr, new Vector2f(br.x, br.y), new Vector3f(index, pixelW, pixelH)));
        structs.add(Vertex.POS3F_UV_DATA3F.create(vtr, new Vector2f(br.x, tl.y), new Vector3f(index, pixelW, pixelH)));
        structs.add(Vertex.POS3F_UV_DATA3F.create(vtl, new Vector2f(tl.x, tl.y), new Vector3f(index, pixelW, pixelH)));

        return structs;
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
