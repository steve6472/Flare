package steve6472.volkaniums.render;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Key;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.struct.def.SBO;
import steve6472.volkaniums.ui.font.*;
import steve6472.volkaniums.MasterRenderer;
import steve6472.volkaniums.VkBuffer;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.descriptors.DescriptorPool;
import steve6472.volkaniums.descriptors.DescriptorSetLayout;
import steve6472.volkaniums.descriptors.DescriptorWriter;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.UBO;
import steve6472.volkaniums.struct.def.Vertex;
import steve6472.volkaniums.ui.font.layout.GlyphInfo;

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
public class FontRenderSystem extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;

    public FontRenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline);

        int fontCount = VolkaniumsRegistries.FONT.keys().size();
        TextureSampler[] fontSamplers = new TextureSampler[fontCount];

        for (Key key : VolkaniumsRegistries.FONT.keys())
        {
            FontEntry fontEntry = VolkaniumsRegistries.FONT.get(key);
            fontSamplers[fontEntry.index()] = VolkaniumsRegistries.SAMPLER.get(key);
        }

        globalSetLayout = DescriptorSetLayout
            .builder(device)
            .addBinding(0, VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, VK_SHADER_STAGE_VERTEX_BIT)
            .addBinding(1, VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, VK_SHADER_STAGE_FRAGMENT_BIT, fontCount)
            .addBinding(2, VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, VK_SHADER_STAGE_FRAGMENT_BIT)
            .build();
        globalPool = DescriptorPool.builder(device)
            .setMaxSets(MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC, MAX_FRAMES_IN_FLIGHT)
            .addPoolSize(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, MAX_FRAMES_IN_FLIGHT)
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

            VkBuffer fontStyles = new VkBuffer(
                device,
                SBO.MSDF_FONT_STYLES.sizeof(),
                1,
                VK_BUFFER_USAGE_STORAGE_BUFFER_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
            fontStyles.map();
            frame.get(i).sboFontStyles = fontStyles;

            frame.get(i).sboFontStyles.writeToBuffer(SBO.MSDF_FONT_STYLES::memcpy, updateFontStylesSBO());

            try (MemoryStack stack = MemoryStack.stackPush())
            {
                DescriptorWriter descriptorWriter = new DescriptorWriter(globalSetLayout, globalPool);
                frame.get(i).descriptorSet = descriptorWriter
                    .writeBuffer(0, stack, frame.get(i).uboBuffer, UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT)
                    .writeImages(1, stack, fontSamplers)
                    .writeBuffer(2, stack, frame.get(i).sboFontStyles)
                    .build();
            }
        }

        buffer = new VkBuffer(
            masterRenderer.getDevice(),
            Vertex.POS3F_UV_FONT_INDEX.sizeof(),
            32768 * 6, // max 32k characters at once, should be enough....
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
        TextRender textRender = getMasterRenderer().textRender();
        //noinspection deprecation
        List<TextLine> lines = textRender.lines();

        if (lines.isEmpty())
            return;

        List<Struct> verticies = createFromChars(lines);

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

        buffer.writeToBuffer(Vertex.POS3F_UV_FONT_INDEX::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), verticies.size(), 1, 0, 0);

        lines.clear();
    }

    private Struct updateFontStylesSBO()
    {
        Struct[] styles = new Struct[VolkaniumsRegistries.FONT_STYLE.keys().size()];
        VolkaniumsRegistries.FONT_STYLE.keys().forEach(key ->
        {
            FontStyleEntry fontStyleEntry = VolkaniumsRegistries.FONT_STYLE.get(key);
            styles[fontStyleEntry.index()] = fontStyleEntry.style().toStruct(fontStyleEntry.style().fontEntry().index());
        });

        return SBO.MSDF_FONT_STYLES.create((Object) styles);
    }

    private List<Struct> createFromChars(List<TextLine> lines)
    {
        List<Struct> structs = new ArrayList<>(lines.size() * 6 * 32);

        for (TextLine line : lines)
        {
            createTextLine(line, structs);
        }
        return structs;
    }

    private void createTextLine(TextLine line, List<Struct> structs)
    {
        FontStyleEntry style = line.style();
        Font font = style.style().font();
        float size = line.size();
        float x = line.startPos().x;
        float y = line.startPos().y;
        float z = line.startPos().z;

        char[] charEntries = line.charEntries();
        for (int i = 0; i < charEntries.length; i++)
        {
            char character = charEntries[i];
            char nextCharacter = i < charEntries.length - 1 ? charEntries[i + 1] : 0;

            GlyphInfo glyphInfo = font.glyphInfo(character);
            float kerningAdvance = font.kerningAdvance(character, nextCharacter);

            if (glyphInfo == null)
                throw new RuntimeException("Glyph for " + character + " (" + ((int) character) + ") not found!");

            if (!glyphInfo.isInvisible())
            {
                createChar(font, glyphInfo, x, y, z, size, structs, style.index());
            }

            x += glyphInfo.advance() * size;
            x += kerningAdvance * size;
        }
    }

    private void createChar(Font font, GlyphInfo glyphInfo, float x, float y, float z, float size, List<Struct> structs, int styleIndex)
    {
        Vector2f tl = new Vector2f(glyphInfo.atlasBounds().left(), glyphInfo.atlasBounds().top()).div(font.getAtlasData().width(), font.getAtlasData().height());
        Vector2f br = new Vector2f(glyphInfo.atlasBounds().right(), glyphInfo.atlasBounds().bottom()).div(font.getAtlasData().width(), font.getAtlasData().height());

        float xpos = x + glyphInfo.planeBounds().left() * size;
        float ypos = y - glyphInfo.planeBounds().bottom() * size;

        float w = glyphInfo.planeBounds().width() * size;
        float h = glyphInfo.planeBounds().height() * size;

        Vector3f vtl = new Vector3f(xpos, ypos + h, z);
        Vector3f vbl = new Vector3f(xpos, ypos, z);
        Vector3f vbr = new Vector3f(xpos + w, ypos, z);
        Vector3f vtr = new Vector3f(xpos + w, ypos + h, z);

        structs.add(Vertex.POS3F_UV_FONT_INDEX.create(vtl, new Vector2f(tl.x, tl.y), styleIndex)); // todo: make it into an SBO, index into it by int(vertexId / 6)
        structs.add(Vertex.POS3F_UV_FONT_INDEX.create(vbl, new Vector2f(tl.x, br.y), styleIndex));
        structs.add(Vertex.POS3F_UV_FONT_INDEX.create(vbr, new Vector2f(br.x, br.y), styleIndex));

        structs.add(Vertex.POS3F_UV_FONT_INDEX.create(vbr, new Vector2f(br.x, br.y), styleIndex));
        structs.add(Vertex.POS3F_UV_FONT_INDEX.create(vtr, new Vector2f(br.x, tl.y), styleIndex));
        structs.add(Vertex.POS3F_UV_FONT_INDEX.create(vtl, new Vector2f(tl.x, tl.y), styleIndex));
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
            flightFrame.sboFontStyles.cleanup();
            if (flightFrame.vertexBuffer != null)
            {
                flightFrame.vertexBuffer.cleanup();
            }
        }
    }

    final static class FlightFrame
    {
        VkBuffer uboBuffer;
        VkBuffer sboFontStyles;
        VkBuffer vertexBuffer;
        long descriptorSet;
    }
}
