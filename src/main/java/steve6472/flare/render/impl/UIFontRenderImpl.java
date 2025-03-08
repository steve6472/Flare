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

    protected void debugSegment(List<TextRenderSegment> messageSegments, int lineNumber, Vector3f origin, Vector2f offset)
    {
        TextRenderSegment segment = messageSegments.get(lineNumber);

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
    }

    protected void debugMessageAnchors(TextRenderSegment first, Vector3f origin, float width, float height)
    {
        // TOP_LEFT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x, origin.y, 1);
        // MIDDLE_LEFT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x, origin.y + height / 2f, 1);
        // BOTTOM_LEFT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x, origin.y + height, 1);

        // TOP_CENTER
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width / 2f, origin.y, 1);
        // MIDDLE_CENTER
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width / 2f, origin.y + height / 2f, 1);
        // BOTTOM_CENTER
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width / 2f, origin.y + height, 1);

        // TOP_RIGHT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width, origin.y, 1);
        // MIDDLE_RIGHT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width, origin.y + height / 2f, 1);
        // BOTTOM_RIGHT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width, origin.y + height, 1);

        float minAscender = first.minAscender;

        // BASELINE_LEFT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x, origin.y - minAscender, 3);
        // BASELINE_CENTER
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width / 2f, origin.y - minAscender, 3);
        // BASELINE_RIGHT
        DebugUILines.MESSAGE_ANCHORS.recrangleAround(origin.x + width, origin.y - minAscender, 3);
    }

    protected void applyAlign(Align align, TextRenderSegment segment, Vector2f offset, float maxWidth)
    {
        if (align == Align.RIGHT)
        {
            offset.x += maxWidth - segment.width;
        } else if (align == Align.CENTER)
        {
            offset.x += maxWidth / 2f - segment.width / 2f;
        }
    }

    protected Vector2f createAnchorOffset(Anchor2D anchor, float width, float height, float minAscender)
    {
        return switch (anchor)
        {
            // TOP_LEFT handled by default
            case MIDDLE_LEFT -> new Vector2f(0, height / 2f);
            case BOTTOM_LEFT -> new Vector2f(0, height);

            case TOP_CENTER -> new Vector2f(width / 2f, 0);
            case MIDDLE_CENTER -> new Vector2f(width / 2f, height / 2f);
            case BOTTOM_CENTER -> new Vector2f(width / 2f, height);

            case TOP_RIGHT -> new Vector2f(width, 0);
            case MIDDLE_RIGHT -> new Vector2f(width, height / 2f);
            case BOTTOM_RIGHT -> new Vector2f(width, height);

            case BASELINE_LEFT -> new Vector2f(0, -minAscender);
            case BASELINE_CENTER -> new Vector2f(width / 2f, -minAscender);
            case BASELINE_RIGHT -> new Vector2f(width, -minAscender);

            default -> new Vector2f();
        };
    }

    protected boolean isNewLine(List<TextRenderSegment> messageSegments, int charIndex)
    {
        for (TextRenderSegment messageSegment : messageSegments)
        {
            if (messageSegment.end == charIndex)
            {
                return true;
            }
        }

        return false;
    }

    protected void renderText(Text message, Matrix4f transform)
    {
        List<TextRenderSegment> messageSegments = message.createSegments();
        final float totalHeight = messageSegments.stream().map(s -> s.fontHeight).collect(FloatUtil.summing(Float::floatValue));
        final float heightForAnchor = message.verticalAnchor() == VerticalAnchorMode.TEXT_HEIGHT ? totalHeight : message.maxHeight();
        Vector3f origin = transform.transformPosition(new Vector3f());
        Vector2f anchorOffset = createAnchorOffset(message.anchor(), message.maxWidth(), heightForAnchor, messageSegments.getFirst().minAscender);
        Vector3f anchorOrigin = transform.transformPosition(new Vector3f()).sub(anchorOffset.x, anchorOffset.y, 0);

        if (DebugUILines.MESSAGE_ORIGIN.get())
        {
            DebugUILines.MESSAGE_ORIGIN.recrangleAround((int) origin.x, (int) origin.y, 2);
        }

        if (DebugUILines.MESSAGE_MAX_WIDTH.get())
        {
            DebugUILines.MESSAGE_MAX_WIDTH.line(new Vector2f(anchorOrigin.x, anchorOrigin.y - 2), new Vector2f(anchorOrigin.x + message.maxWidth(), anchorOrigin.y - 2));
        }

        if (DebugUILines.MESSAGE_MAX_HEIGHT.get())
        {
            DebugUILines.MESSAGE_MAX_HEIGHT.line(new Vector2f(anchorOrigin.x - 2, anchorOrigin.y), new Vector2f(anchorOrigin.x - 2, anchorOrigin.y + message.maxHeight()));
        }

        if (DebugUILines.MESSAGE_ANCHORS.get())
        {
            debugMessageAnchors(messageSegments.getFirst(), new Vector3f(anchorOrigin), message.maxWidth(), heightForAnchor);
        }

        Vector2f offset = new Vector2f();
        offset.x -= anchorOffset.x;
        offset.y -= anchorOffset.y;

        final int CHAR_INDEX = 0;
        final int LINE_NUMBER = 1;
        int[] indicies = {0, 0};

        // Apply align for the first line
        if (!message.forceSingleLine())
            applyAlign(message.align(), messageSegments.getFirst(), offset, message.maxWidth());

        debugSegment(messageSegments, indicies[LINE_NUMBER], origin, offset);

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

            boolean newLine = isNewLine(messageSegments, indicies[CHAR_INDEX]);
            if (newLine)
            {
                offset.x = 0;
                indicies[LINE_NUMBER]++;
                if (!message.forceSingleLine())
                    applyAlign(message.align(), messageSegments.get(indicies[1]), offset, message.maxWidth());
                offset.x -= anchorOffset.x;

                offset.y += messageSegments.get(indicies[LINE_NUMBER] - 1).maxLineHeight + message.lineGapOffset() * size;

                debugSegment(messageSegments, indicies[LINE_NUMBER], origin, offset);
            }

            if (!glyph.isInvisible() || DebugUILines.anyCharacterDebugOn())
            {
                TextRenderSegment segment = messageSegments.get(indicies[LINE_NUMBER]);
                float offsetY = 0;
                // Moves the font so that the baseline is in line with the origin
                offsetY += glyph.planeBounds().top() * size;
                offsetY -= segment.minAscender;
                renderChar(font, glyph, new Vector2f(offset).add(0, offsetY), size, character.style().index(), transform);
            }

            offset.x += glyph.advance() * size;
            offset.x += kerningAdvance * size;

            if (DebugUILines.CHARACTER_KERN.get() && kerningAdvance != 0.0f)
            {
                int h = (int) ((glyph.planeBounds().height() + nextCharacter.glyph().planeBounds().height()) * size / 4f);

                Vector2f v = new Vector2f(origin).add(offset);
                DebugUILines.CHARACTER_KERN.rectangle(new Vector2i(v, RoundingMode.TRUNCATE).add(0, h + 6), new Vector2i(v, RoundingMode.TRUNCATE).add((int) (-kerningAdvance * size), h + 8));
            }

            indicies[CHAR_INDEX]++;
        });
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
