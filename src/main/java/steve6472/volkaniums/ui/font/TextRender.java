package steve6472.volkaniums.ui.font;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.core.registry.Key;
import steve6472.volkaniums.ui.font.layout.GlyphInfo;
import steve6472.volkaniums.ui.font.layout.Kerning;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 */
public class TextRender
{
    private final FontInfo fontInfo;
    private final List<TextLine> lines = new ArrayList<>(64);

    public TextRender()
    {
        this.fontInfo = new FontInfo();
    }

    /*
     * Rendering functions
     */

    public void line(String text, float size, Vector3f position, Vector4f color)
    {
        lines.add(TextLine.fromText(text, size, position));
    }

    public void centeredLine(String text, float size, Vector3f center, Vector4f color)
    {
        lines.add(TextLine.fromText(text, size, new Vector3f(center).sub(getWidth(text, size) / 2f, 0, 0)));
    }

    public void centeredLine(String text, float size, Vector3f center, Vector4f color, Key style)
    {
        lines.add(TextLine.fromText(text, size, new Vector3f(center).sub(getWidth(text, size) / 2f, 0, 0), style));
    }

    /*
     * Util functions
     */

    public GlyphInfo glyphInfo(long character)
    {
        GlyphInfo glyphInfo = fontInfo.glyphs.get(character);
        if (glyphInfo == null)
            return errorGlyph();
        return glyphInfo;
    }

    public float kerningAdvance(char left, char right)
    {
        Long2FloatMap leftKern = fontInfo.kerning.get(left);
        if (leftKern == null)
            return 0f;

        return leftKern.get(right);
    }

    public GlyphInfo errorGlyph()
    {
        return fontInfo.ERROR;
    }

    public float getWidth(String text, float size)
    {
        float width = 0;

        for (char c : text.toCharArray())
        {
            GlyphInfo glyphInfo = glyphInfo(c);
            width += glyphInfo.advance() * size;
        }

        return width;
    }

    /*
     * Internal functions
     */

    /// Deprecated - internal
    @Deprecated
    public FontInfo fontInfo()
    {
        return fontInfo;
    }

    /// Deprecated - internal
    @Deprecated
    public List<TextLine> lines()
    {
        return lines;
    }
}
