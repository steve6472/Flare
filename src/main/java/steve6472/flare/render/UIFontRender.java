package steve6472.flare.render;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Key;
import steve6472.flare.Camera;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.descriptors.DescriptorPool;
import steve6472.flare.descriptors.DescriptorSetLayout;
import steve6472.flare.descriptors.DescriptorWriter;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.impl.UIFontRenderImpl;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.SBO;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.ui.font.Font;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.UnknownCharacter;
import steve6472.flare.ui.font.layout.GlyphInfo;
import steve6472.flare.ui.font.render.*;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.util.MatrixAnim;

import java.nio.LongBuffer;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.flare.SwapChain.MAX_FRAMES_IN_FLIGHT;

/**
 * Created by steve6472
 * Date: 12/11/2024
 * Project: MoonDust <br>
 */
public class UIFontRender extends RenderSystem
{
    private final DescriptorPool globalPool;
    private final DescriptorSetLayout globalSetLayout;
    List<FlightFrame> frame = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
    private final VkBuffer buffer;
    private final UIFontRenderImpl renderImpl;

    public UIFontRender(MasterRenderer masterRenderer, UIFontRenderImpl renderImpl)
    {
        super(masterRenderer, Pipelines.FONT_SDF);
        Objects.requireNonNull(renderImpl);
        this.renderImpl = renderImpl;

        int fontCount = FlareRegistries.FONT.keys().size();
        TextureSampler[] fontSamplers = new TextureSampler[fontCount];

        for (Key key : FlareRegistries.FONT.keys())
        {
            FontEntry fontEntry = FlareRegistries.FONT.get(key);
            fontSamplers[fontEntry.index()] = FlareRegistries.SAMPLER.get(key);
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
            vertex().sizeof(),
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

    public int lastVertexCount = 0;

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        List<Struct> verticies = null;

        if (frameInfo.camera().cameraIndex == 0)
        {
            verticies = new ArrayList<>();
            renderImpl.setStructList(verticies);
            renderImpl.render();

            if (verticies.isEmpty())
                return;

            lastVertexCount = verticies.size();
        }

        FlightFrame flightFrame = frame.get(frameInfo.frameIndex());

        Camera camera = new Camera();
        int windowWidth = this.getMasterRenderer().getWindow().getWidth();
        int windowHeight = this.getMasterRenderer().getWindow().getHeight();
        camera.setOrthographicProjection(0.0F, (float)windowWidth, 0.0F, (float)windowHeight, 0.0F, renderImpl.getFar());
        camera.setViewYXZ(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(0.0F, 0.0F, 0.0F));
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

        if (verticies != null)
            buffer.writeToBuffer(vertex()::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), lastVertexCount, 1, 0, 0);
    }

    private Struct updateFontStylesSBO()
    {
        Collection<Key> keys = FlareRegistries.FONT_STYLE.keys();
        Struct[] styles = new Struct[keys.size()];
        keys.forEach(key ->
        {
            FontStyleEntry fontStyleEntry = FlareRegistries.FONT_STYLE.get(key);
            styles[fontStyleEntry.index()] = fontStyleEntry.style().toStruct(fontStyleEntry.style().fontEntry());
        });

        return SBO.MSDF_FONT_STYLES.create((Object) styles);
    }

    private List<Struct> createFromChars(List<TextLineObject> lines, List<TextMessageObject> messages, Camera camera)
    {
        List<Struct> structs = new ArrayList<>(lines.size() * 6 * 32);

        for (TextLineObject object : lines)
        {
            createTextLine(object, camera, structs);
        }

        for (TextMessageObject object : messages)
        {
            createTextMessage(object, camera, structs);
        }
        return structs;
    }

    private void createTextLine(TextLineObject textObject, Camera camera, List<Struct> structs)
    {
        TextLine line = textObject.line();
        FontStyleEntry style = line.style();
        Font font = style.style().font();
        float size = line.size();

        Vector2f alignOffset = new Vector2f();
        line.anchor().applyOffset(alignOffset, font.getWidth(line.charEntries(), size), font.getMetrics().ascender() * size, font.getMetrics().descender() * size);

        Matrix4f transform = new Matrix4f();
        float animTime = MatrixAnim.getAnimTime(textObject.startTime(), textObject.endTime(), System.currentTimeMillis());
        MatrixAnim.animate(textObject.transformFrom(), textObject.transformTo(), animTime, transform);

        line.billboard().apply(camera, transform);
        transform.translate(alignOffset.x, alignOffset.y, 0);

        Vector2f offset = new Vector2f();
        char[] charEntries = line.charEntries();
        for (int i = 0; i < charEntries.length; i++)
        {
            char character = charEntries[i];
            char nextCharacter = i < charEntries.length - 1 ? charEntries[i + 1] : 0;

            GlyphInfo glyphInfo = font.glyphInfo(character);
            float kerningAdvance = font.kerningAdvance(character, nextCharacter);

            if (!glyphInfo.isInvisible())
            {
                createChar(font, glyphInfo, offset, size, structs, style.index(), transform);
            }

            offset.x += glyphInfo.advance() * size;

            if (i < charEntries.length - 1)
                offset.x += kerningAdvance * size;
        }
    }

    private void createTextMessage(TextMessageObject textObject, Camera camera, List<Struct> structs)
    {
        TextMessage message = textObject.message();

        Matrix4f transform = new Matrix4f();
        float animTime = MatrixAnim.getAnimTime(textObject.startTime(), textObject.endTime(), System.currentTimeMillis());
        MatrixAnim.animate(textObject.transformFrom(), textObject.transformTo(), animTime, transform);

        // TODO: line breaking
        BreakIterator breakIterator = BreakIterator.getLineInstance();
        StringBuilder bobTheBuilder = new StringBuilder();
        textObject.message().lines().forEach(l -> bobTheBuilder.append(l.charEntries()));
        breakIterator.setText(bobTheBuilder.toString());

        int current = breakIterator.next();
        int previous = 0;
        IntList breakIndicies = new IntArrayList();
        float totalWidth = 0;
        float maxWidth = 0;
        while (current != BreakIterator.DONE)
        {
            float width = message.getWidth(previous, current);
            totalWidth += width;

            if (totalWidth > message.maxWidth())
            {
                breakIndicies.add(previous);
                maxWidth = Math.max(maxWidth, totalWidth - width);
                totalWidth = width;
            }

            previous = current;
            current = breakIterator.next();
        }

        //        Vector2f alignOffset = new Vector2f();
        //        message.anchor().applyOffset(alignOffset, maxWidth, font.getMetrics().ascender() * messageSize, font.getMetrics().descender() * messageSize);

        message.billboard().apply(camera, transform);
        //        transform.translate(alignOffset.x, alignOffset.y, 0);

        Vector2f offset = new Vector2f();
        int[] charIndex = {0};

        message.iterateCharacters((character, nextCharacter) ->
        {
            if (character.glyph() == null)
                throw new RuntimeException("Glyph for some character not found not found!");

            Font font = character.style().style().font();

            float kerningAdvance = 0f;
            if (nextCharacter != null && font == nextCharacter.style().style().font())
            {
                kerningAdvance = font.kerningAdvance((char) character.glyph().index(), (char) nextCharacter.glyph().index());
            }

            if (breakIndicies.contains(charIndex[0]))
            {
                int lineNum = (breakIndicies.indexOf(charIndex[0]) + 1);
                float lineHeight = font.getMetrics().ascender() - font.getMetrics().descender() + 0;
                offset.set(0, lineNum * -lineHeight * character.size());
            }

            if (!character.glyph().isInvisible())
            {
                createChar(font, character.glyph(), offset, character.size(), structs, character.style().index(), transform);
            }

            offset.x += character.glyph().advance() * character.size();
            offset.x += kerningAdvance * character.size();

            charIndex[0]++;
        });
    }

    private void createChar(Font font, GlyphInfo glyphInfo, Vector2f offset, float size, List<Struct> structs, int styleIndex, Matrix4f transform)
    {
        if (glyphInfo == UnknownCharacter.unknownGlyph())
        {
            font = UnknownCharacter.fontEntry().font();
            styleIndex = UnknownCharacter.styleEntry().index();
        }

        Vector2f tl = new Vector2f(glyphInfo.atlasBounds().left(), glyphInfo.atlasBounds().top()).div(font.getAtlasData().width(), font.getAtlasData().height());
        Vector2f br = new Vector2f(glyphInfo.atlasBounds().right(), glyphInfo.atlasBounds().bottom()).div(font.getAtlasData().width(), font.getAtlasData().height());

        float xpos = offset.x + glyphInfo.planeBounds().left() * size;
        float ypos = offset.y - glyphInfo.planeBounds().height() * size + font.getMetrics().lineHeight() * size;

        float w = glyphInfo.planeBounds().width() * size;
        float h = glyphInfo.planeBounds().height() * size;

        Vector3f vtl = new Vector3f(xpos, ypos, 0).mulPosition(transform);
        Vector3f vbl = new Vector3f(xpos, ypos + h, 0).mulPosition(transform);
        Vector3f vbr = new Vector3f(xpos + w, ypos + h, 0).mulPosition(transform);
        Vector3f vtr = new Vector3f(xpos + w, ypos, 0).mulPosition(transform);

        structs.add(vertex().create(vtl, new Vector2f(tl.x, tl.y), styleIndex));
        structs.add(vertex().create(vbl, new Vector2f(tl.x, br.y), styleIndex));
        structs.add(vertex().create(vbr, new Vector2f(br.x, br.y), styleIndex));

        structs.add(vertex().create(vbr, new Vector2f(br.x, br.y), styleIndex));
        structs.add(vertex().create(vtr, new Vector2f(br.x, tl.y), styleIndex));
        structs.add(vertex().create(vtl, new Vector2f(tl.x, tl.y), styleIndex));
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