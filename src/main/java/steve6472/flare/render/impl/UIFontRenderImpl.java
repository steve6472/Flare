package steve6472.flare.render.impl;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.core.registry.Key;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.struct.def.Vertex;
import steve6472.flare.ui.font.Font;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.UnknownCharacter;
import steve6472.flare.ui.font.layout.GlyphInfo;
import steve6472.flare.ui.font.render.*;
import steve6472.flare.ui.font.style.FontStyle;
import steve6472.flare.ui.font.style.FontStyleEntry;
import steve6472.flare.util.FloatUtil;

import java.util.List;

/**
 * Created by steve6472
 * Date: 12/12/2024
 * Project: Flare <br>
 */
public abstract class UIFontRenderImpl extends RenderImpl
{
    private final float far;

    public UIFontRenderImpl(float far)
    {
        this.far = far;
    }

    protected final void vertex(Vector3f position, Vector2f uv, int index)
    {
        structList.add(Vertex.POS3F_UV_INDEX.create(position, uv, index));
    }

    protected void renderLine(UITextLine line, int x, int y)
    {
        renderLine(line, new Matrix4f().translate(x, y, 0));
    }

    protected void renderMessage(UITextMessage message, Matrix4f transform)
    {
        List<UIMessageSegment> messageSegments = message.createSegments();
        final float maxWidth = messageSegments.stream().map(s -> s.width).max(Float::compare).orElse(0f);
        final float totalHeight = messageSegments.stream().map(s -> s.height).collect(FloatUtil.summing(Float::floatValue));

        Vector2f offset = new Vector2f();
        // charIndex, lineNumber
        int[] indicies = {0, 0};

        if (message.align() == Align.RIGHT)
        {
            message.anchor().applyOffset(offset, maxWidth, 0, totalHeight);
            offset.x += maxWidth - messageSegments.getFirst().width;
        } else if (message.align() == Align.CENTER)
        {
            message.anchor().applyOffset(offset, maxWidth, 0, totalHeight);
            offset.x += maxWidth / 2f - messageSegments.getFirst().width / 2f;
        }

        message.iterateCharacters((character, nextCharacter) ->
        {
            GlyphInfo glyph = character.glyph();
            float size = character.size();

            if (glyph == null)
                throw new RuntimeException("Glyph for some character not found not found!");

            Font font = character.style().style().font();

            float kerningAdvance = 0f;
            if (nextCharacter != null && font == nextCharacter.style().style().font())
            {
                kerningAdvance = font.kerningAdvance((char) glyph.index(), (char) nextCharacter.glyph().index());
            }

            // Todo: somehow handle breakIndicies of 0 at index 0
            boolean newLine = false;
            for (UIMessageSegment messageSegment : messageSegments)
            {
                if (messageSegment.end == indicies[0])
                {
                    newLine = true;
                    break;
                }
            }
            if (newLine)
            {
                offset.x = 0;
                indicies[1]++;
                if (message.align() == Align.RIGHT)
                {
                    Vector2f offTemp = new Vector2f();
                    message.anchor().applyOffset(offTemp, maxWidth, 0, totalHeight);
                    offset.x = offTemp.x;
                    offset.x += maxWidth - messageSegments.get(indicies[1]).width;
                } else if (message.align() == Align.CENTER)
                {
                    Vector2f offTemp = new Vector2f();
                    message.anchor().applyOffset(offTemp, maxWidth, 0, totalHeight);
                    offset.x = offTemp.x;
                    offset.x += maxWidth / 2f - messageSegments.get(indicies[1]).width / 2f;
                }

                if (message.newLineType() == NewLineType.MAX_HEIGHT)
                    offset.y += messageSegments.get(indicies[1] - 1).height + messageSegments.get(indicies[1] - 1).maxDescent - messageSegments.get(indicies[1]).minDescent;
                else if (message.newLineType() == NewLineType.FIXED)
                    offset.y += font.getMetrics().lineHeight() * size + message.lineGapOffset() * size;
            }

            if (!glyph.isInvisible())
            {
                UIMessageSegment segment = messageSegments.get(indicies[1]);
                float offsetY = segment.height - glyph.planeBounds().height() * size;
                offsetY -= segment.minDescent - glyph.planeBounds().bottom() * size;
                renderChar(font, glyph, new Vector2f(offset).add(0, offsetY), size, character.style().index(), transform);
            }

            offset.x += glyph.advance() * size;
            offset.x += kerningAdvance * size;

            indicies[0]++;
        });
    }

    protected void renderLine(UITextLine text, Matrix4f transform)
    {
        renderLine(text.text(), text.size(), text.style(), text.anchor(), transform);
    }

    protected void renderLine(String text, float size, FontStyleEntry style, Anchor2D anchor, Matrix4f transform)
    {
        Font font = style.style().font();
        float maxHeight = font.getMaxHeight(text, size);
        float textWidth = font.getWidth(text, size);

        Vector2f alignOffset = new Vector2f();
        anchor.applyOffset(alignOffset, textWidth, 0, maxHeight);
        Matrix4f mat = new Matrix4f(transform);
        mat.translate(alignOffset.x, alignOffset.y, 0);

        float cumAdvance = 0;
        for (char c : text.toCharArray())
        {
            GlyphInfo glyphInfo = font.glyphInfo(c);
            float offsetY = maxHeight - glyphInfo.planeBounds().height() * size;
            renderChar(font, glyphInfo, new Vector2f(cumAdvance, offsetY), size, style.index(), new Matrix4f().mul(mat));
            cumAdvance += glyphInfo.advance() * size;
        }
    }

    protected void renderChar(Font font, GlyphInfo glyphInfo, Vector2f offset, float size, int styleIndex, Matrix4f transform)
    {
        if (glyphInfo == UnknownCharacter.unknownGlyph())
        {
            font = UnknownCharacter.fontEntry().font();
            styleIndex = UnknownCharacter.styleEntry().index();
        }

        Vector2f tl = new Vector2f(glyphInfo.atlasBounds().left(), glyphInfo.atlasBounds().top()).div(font.getAtlasData().width(), font.getAtlasData().height());
        Vector2f br = new Vector2f(glyphInfo.atlasBounds().right(), glyphInfo.atlasBounds().bottom()).div(font.getAtlasData().width(), font.getAtlasData().height());

        float xpos = offset.x;
        float ypos = offset.y;

        float w = glyphInfo.planeBounds().width() * size;
        float h = glyphInfo.planeBounds().height() * size;

        Vector3f vtl = new Vector3f(xpos, ypos, 0).mulPosition(transform);
        Vector3f vbl = new Vector3f(xpos, ypos + h, 0).mulPosition(transform);
        Vector3f vbr = new Vector3f(xpos + w, ypos + h, 0).mulPosition(transform);
        Vector3f vtr = new Vector3f(xpos + w, ypos, 0).mulPosition(transform);

        vertex(vtl, new Vector2f(tl.x, tl.y), styleIndex);
        vertex(vbl, new Vector2f(tl.x, br.y), styleIndex);
        vertex(vbr, new Vector2f(br.x, br.y), styleIndex);

        vertex(vbr, new Vector2f(br.x, br.y), styleIndex);
        vertex(vtr, new Vector2f(br.x, tl.y), styleIndex);
        vertex(vtl, new Vector2f(tl.x, tl.y), styleIndex);
    }

    protected final FontStyleEntry getStyleEntry(Key styleKey)
    {
        return FlareRegistries.FONT_STYLE.get(styleKey);
    }

    protected final FontStyle getStyle(Key styleKey)
    {
        return getStyleEntry(styleKey).style();
    }

    protected final FontEntry getFontEntry(Key fontKey)
    {
        return FlareRegistries.FONT.get(fontKey);
    }

    protected final Font getFont(Key fontKey)
    {
        return getFontEntry(fontKey).font();
    }

    public float getFar()
    {
        return far;
    }
}
