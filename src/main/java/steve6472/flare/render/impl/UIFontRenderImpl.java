package steve6472.flare.render.impl;

import org.joml.*;
import steve6472.core.registry.Key;
import steve6472.core.util.RandomUtil;
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
import steve6472.test.DebugUILines;

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
        Vector3f origin = transform.transformPosition(new Vector3f());
        if (DebugUILines.MESSAGE_ORIGIN.get())
        {
            DebugUILines.MESSAGE_ORIGIN.rectangle(new Vector2i((int) origin.x - 1, (int) origin.y - 1), new Vector2i((int) origin.x + 1, (int) origin.y + 1));
        }

        if (DebugUILines.MESSAGE_MAX_WIDTH.get())
        {
            DebugUILines.MESSAGE_MAX_WIDTH.line(new Vector2i((int) origin.x, (int) origin.y - 2), new Vector2i((int) (origin.x + message.maxWidth()), (int) origin.y - 2));
        }

        List<UIMessageSegment> messageSegments = message.createSegments();
        final float maxWidth = messageSegments.stream().map(s -> s.width).max(Float::compare).orElse(0f);
        final float totalHeight = messageSegments.stream().map(s -> s.fontHeight).collect(FloatUtil.summing(Float::floatValue));

        Vector2f offset = new Vector2f();
        // charIndex, lineNumber
        int[] indicies = {0, 0};

//        if (message.align() == Align.RIGHT)
//        {
//            message.anchor().applyOffset(offset, maxWidth, 0, totalHeight);
//            offset.x += maxWidth - messageSegments.getFirst().width;
//        } else if (message.align() == Align.CENTER)
//        {
//            message.anchor().applyOffset(offset, maxWidth, 0, totalHeight);
//            offset.x += maxWidth / 2f - messageSegments.getFirst().width / 2f;
//        }

        Runnable debugSegment = () ->
        {
            UIMessageSegment segment = messageSegments.get(indicies[1]);

            // Box pointing up for Max Descent
            Vector2f v = new Vector2f(origin).add(offset).add(0, segment.maxDescent);
            Vector2i r = new Vector2i(v, RoundingMode.TRUNCATE);
            if (DebugUILines.SEGMENT_MAX_DESCENT.get())
            {
                DebugUILines.SEGMENT_MAX_DESCENT.rectangle(r, new Vector2i(r).add((int) (segment.width), 4));
                DebugUILines.SEGMENT_MAX_DESCENT.rectangle(new Vector2i(r).add(0, -4), new Vector2i(r).add(4, 4));
            }

            if (DebugUILines.SEGMENT_MIN_DESCENT.get())
            {
                // Box pointing down for Min Descent
                v = new Vector2f(origin).add(offset).add(0, segment.minDescent);
                r = new Vector2i(v, RoundingMode.TRUNCATE);
                DebugUILines.SEGMENT_MIN_DESCENT.rectangle(r, new Vector2i(r).add((int) (segment.width), 4));
                DebugUILines.SEGMENT_MIN_DESCENT.rectangle(r, new Vector2i(r).add(4, 8));
            }

            if (DebugUILines.SEGMENT.get())
            {
                // Segment bounding box
                v = new Vector2f(origin).add(offset);
                DebugUILines.SEGMENT.rectangle(new Vector2i(v, RoundingMode.TRUNCATE), new Vector2i(v, RoundingMode.TRUNCATE).add((int) segment.width, (int) segment.fontHeight));
            }
        };
        debugSegment.run();

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
//                if (message.align() == Align.RIGHT)
//                {
//                    Vector2f offTemp = new Vector2f();
//                    message.anchor().applyOffset(offTemp, maxWidth, 0, totalHeight);
//                    offset.x = offTemp.x;
//                    offset.x += maxWidth - messageSegments.get(indicies[1]).width;
//                } else if (message.align() == Align.CENTER)
//                {
//                    Vector2f offTemp = new Vector2f();
//                    message.anchor().applyOffset(offTemp, maxWidth, 0, totalHeight);
//                    offset.x = offTemp.x;
//                    offset.x += maxWidth / 2f - messageSegments.get(indicies[1]).width / 2f;
//                }

//                if (message.newLineType() == NewLineType.MAX_HEIGHT)
//                    offset.y += messageSegments.get(indicies[1] - 1).height + messageSegments.get(indicies[1] - 1).maxDescent - messageSegments.get(indicies[1]).minDescent;
//                else if (message.newLineType() == NewLineType.FIXED)
                offset.y += messageSegments.get(indicies[1] - 1).maxLineHeight + message.lineGapOffset() * size;

                debugSegment.run();
            }


            // TODO: underline rendering has to work even with invisible characters.. I think
//            if (!glyph.isInvisible())
            {
                UIMessageSegment segment = messageSegments.get(indicies[1]);
                float offsetY = 0;
                // Moves the font so that the baseline is in line with the origin
                offsetY += glyph.planeBounds().top() * size;
//                offsetY -= font.getMetrics().ascender() * size;
                offsetY -= segment.minAscender;
//                offsetY = segment.fontHeight - glyph.planeBounds().height() * size;
//                offsetY += glyph.planeBounds().bottom() * size;
//                offsetY += -segment.maxDescent;
                //                offsetY -= seg1ment.minDescent - glyph.planeBounds().bottom() * size;
//                offsetY -= glyph.planeBounds().bottom() * size;
                renderChar(font, glyph, new Vector2f(offset).add(0, offsetY), size, character.style().index(), transform);
            }

            offset.x += glyph.advance() * size;
            offset.x += kerningAdvance * size;

            if (DebugUILines.CHARACTER_KERN.get() && kerningAdvance != 0.0f)
            {
//                System.out.printf("Kerning for %s - %s : %s%n", (char) glyph.index(), (char) nextCharacter.glyph().index(), kerningAdvance);
                int h = (int) ((glyph.planeBounds().height() + nextCharacter.glyph().planeBounds().height()) * size / 4f);

                Vector2f v = new Vector2f(origin).add(offset);
                DebugUILines.CHARACTER_KERN.rectangle(new Vector2i(v, RoundingMode.TRUNCATE).add(0, h + 6), new Vector2i(v, RoundingMode.TRUNCATE).add((int) (-kerningAdvance * size), h + 8));
            }

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

        // UV
        Vector2f tl = new Vector2f(glyphInfo.atlasBounds().left(), glyphInfo.atlasBounds().top()).div(font.getAtlasData().width(), font.getAtlasData().height());
        Vector2f br = new Vector2f(glyphInfo.atlasBounds().right(), glyphInfo.atlasBounds().bottom()).div(font.getAtlasData().width(), font.getAtlasData().height());

        float xpos = offset.x;
        float ypos = offset.y;
        float leftFix = glyphInfo.planeBounds().left() * size;

        xpos += leftFix;

        // Use advance to fake width for further debug rendering
        float w = glyphInfo.isInvisible() ? glyphInfo.advance() * size : glyphInfo.planeBounds().width() * size;
        float h = glyphInfo.planeBounds().height() * size;

        Vector3f vtl = new Vector3f(xpos, ypos, -1).mulPosition(transform);
        Vector3f vbl = new Vector3f(xpos, ypos + h, -1).mulPosition(transform);
        Vector3f vbr = new Vector3f(xpos + w, ypos + h, -1).mulPosition(transform);
        Vector3f vtr = new Vector3f(xpos + w, ypos, -1).mulPosition(transform);

        if (!glyphInfo.isInvisible())
        {
            vertex(vtl, new Vector2f(tl.x, tl.y), styleIndex);
            vertex(vbl, new Vector2f(tl.x, br.y), styleIndex);
            vertex(vbr, new Vector2f(br.x, br.y), styleIndex);

            vertex(vbr, new Vector2f(br.x, br.y), styleIndex);
            vertex(vtr, new Vector2f(br.x, tl.y), styleIndex);
            vertex(vtl, new Vector2f(tl.x, tl.y), styleIndex);
        }

        if (DebugUILines.CHARACTER.get())
        {
            DebugUILines.CHARACTER.rectangle(new Vector2i((int) vtl.x, (int) vtl.y), new Vector2i((int) vbr.x, (int) vbr.y));
        }

        if (DebugUILines.CHARACTER_ASCENT.get())
        {
            DebugUILines.CHARACTER_ASCENT.rectangle(
                new Vector2i(
                    (int) (vtl.x - 2 + w / 2 - leftFix / 2f),
                    (int) vtl.y),
                new Vector2i(
                    (int) (vtl.x - 4 + w / 2 + (glyphInfo.planeBounds().top() < 0 ? 0 : -4) - leftFix / 2f),
                    (int) (vtl.y - glyphInfo.planeBounds().top() * size)
                ));
        }

        if (DebugUILines.CHARACTER_DESCENT.get())
        {
            DebugUILines.CHARACTER_DESCENT.rectangle(
                new Vector2i(
                    (int) (vtr.x + 2 - w / 2 - leftFix / 2f),
                    (int) (vtr.y - glyphInfo.planeBounds().top() * size)),
                new Vector2i(
                    (int) (vtr.x + 4 - w / 2 + (glyphInfo.planeBounds().bottom() < 0 ? 4 : 0) - leftFix / 2f),
                    (int) (vtr.y + glyphInfo.planeBounds().bottom() * size - glyphInfo.planeBounds().top() * size)
                ));
        }

        if (DebugUILines.BASELINE.get())
        {
            float baseline = glyphInfo.planeBounds().top() * size;
            DebugUILines.BASELINE.line(new Vector2i((int) (vtl.x - leftFix), (int) vtl.y).add(0, (int) -baseline), new Vector2i((int) (vtl.x), (int) vtl.y).add((int) w, (int) -baseline));
        }

        if (DebugUILines.CHARACTER_ADVANCE.get())
        {
            float baseline = glyphInfo.planeBounds().top() * size;
            int rng = RandomUtil.randomInt(-16, -2, glyphInfo.hashCode() + glyphInfo.index());
            // TODO: this is negative, fix (visibly shows incorrect box)
            DebugUILines.CHARACTER_ADVANCE.rectangle(new Vector2i((int) (vtl.x - leftFix), (int) vtl.y).add(0, (int) -baseline + rng), new Vector2i((int) (vtl.x - leftFix), (int) vtl.y).add((int) (glyphInfo.advance() * size), (int) -baseline + rng - 2));
        }

        if (DebugUILines.CHARACTER_UNDERLINE.get())
        {
            float baseline = glyphInfo.planeBounds().top() * size;
            float thickness = font.getMetrics().underlineThickness();
            float underlineY = font.getMetrics().underlineY() - thickness / 2f;
//            DebugUILines.CHARACTER_UNDERLINE.rectangle(new Vector2i((int) (vtl.x - leftFix), (int) vtl.y).add(0, (int) (-baseline + underlineY * size)), new Vector2i((int) (vtl.x - leftFix), (int) vtl.y).add((int) w, (int) (-baseline + underlineY * size + thickness * size)));
            DebugUILines.CHARACTER_UNDERLINE.rectangle(new Vector2i((int) vtl.x, (int) vtl.y).add(0, (int) (-baseline + underlineY * size)), new Vector2i((int) vtl.x, (int) vtl.y).add((int) w, (int) (-baseline + underlineY * size + thickness * size)));
        }
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
