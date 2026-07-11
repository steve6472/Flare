package steve6472.flare.render;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Holder;
import steve6472.flare.Camera;
import steve6472.flare.MasterRenderer;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.render.common.CommonBuilder;
import steve6472.flare.render.common.CommonRenderSystem;
import steve6472.flare.render.common.FlightFrame;
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
import java.util.List;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 12/11/2024
 * Project: MoonDust <br>
 */
public class UIFontRender extends CommonRenderSystem
{
    private static final int FONT_STYLES_INDEX = 1;

    private final VkBuffer buffer;
    private final UIFontRenderImpl renderImpl;

    public UIFontRender(MasterRenderer masterRenderer, UIFontRenderImpl renderImpl)
    {
        Objects.requireNonNull(renderImpl);

        int fontCount = (int) BuiltInFlareRegistries.FONT.listElements().count();
        @SuppressWarnings("unchecked")
        Holder<TextureSampler>[] fontSamplers = new Holder[fontCount];

        BuiltInFlareRegistries.FONT.listElements().forEach(ref -> {
            FontEntry fontEntry = ref.value();
            fontSamplers[fontEntry.index()] = BuiltInFlareRegistries.SAMPLER.get(ref.key().resource()).orElseThrow();
        });

        super(masterRenderer,
            Pipelines.FONT_SDF,
            CommonBuilder.create()
                .entryImages(fontSamplers)
                .entrySBO(SBO.MSDF_FONT_STYLES.sizeof(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_SHADER_STAGE_FRAGMENT_BIT)
                .postCreation(ff -> ff.getBuffer(FONT_STYLES_INDEX).writeToBuffer(SBO.MSDF_FONT_STYLES::memcpy, updateFontStylesSBO())));
        this.renderImpl = renderImpl;

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
    protected void onReload(FlightFrame frame)
    {
        super.onReload(frame);
        frame.getBuffer(FONT_STYLES_INDEX).writeToBuffer(SBO.MSDF_FONT_STYLES::memcpy, updateFontStylesSBO());
    }

    public int lastVertexCount = 0;

    @Override
    public void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack)
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

        if (verticies != null)
            buffer.writeToBuffer(vertex()::memcpy, verticies);

        LongBuffer vertexBuffers = stack.longs(buffer.getBuffer());
        LongBuffer offsets = stack.longs(0);
        vkCmdBindVertexBuffers(frameInfo.commandBuffer(), 0, vertexBuffers, offsets);
        vkCmdDraw(frameInfo.commandBuffer(), lastVertexCount, 1, 0, 0);
    }

    @Override
    protected void updateData(FlightFrame flightFrame, FrameInfo frameInfo)
    {
    }

    @Override
    protected void setupCameraUbo(FlightFrame flightFrame, Camera camera)
    {
        Camera orthoCamera = new Camera();
        int windowWidth = getMasterRenderer().getWindow().getWidth();
        int windowHeight = getMasterRenderer().getWindow().getHeight();

        orthoCamera.setOrthographicProjection(0, windowWidth, 0, windowHeight, 0f, renderImpl.getFar());
        orthoCamera.setViewYXZ(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0));

        Struct globalUBO = UBO.GLOBAL_CAMERA_UBO.create(orthoCamera.getProjectionMatrix(), orthoCamera.getViewMatrix());
        int singleInstanceSize = UBO.GLOBAL_CAMERA_UBO.sizeof() / UBO.GLOBAL_CAMERA_MAX_COUNT;

        flightFrame.cameraUbo.writeToBuffer(UBO.GLOBAL_CAMERA_UBO::memcpy, List.of(globalUBO), singleInstanceSize, singleInstanceSize * camera.cameraIndex);
        flightFrame.cameraUbo.flush(singleInstanceSize, (long) singleInstanceSize * camera.cameraIndex);
    }

    private static Struct updateFontStylesSBO()
    {
        int count = (int) BuiltInFlareRegistries.FONT_STYLE.listElements().count();

        Struct[] styles = new Struct[count];
        BuiltInFlareRegistries.FONT_STYLE.listElements().forEach(ref -> {
            FontStyleEntry value = ref.value();
            styles[value.index()] = value.style().toStruct(value.style().fontEntry().value());
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
        FontStyleEntry style = line.style().value();
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
        super.cleanup();

        if (buffer != null)
            buffer.cleanup();
    }
}