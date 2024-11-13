package steve6472.volkaniums.ui.font;

import org.joml.Vector3f;
import steve6472.core.registry.Key;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.ui.font.style.FontStyle;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 */
public class TextRender
{
    private final List<TextLine> lines = new ArrayList<>(256);

    public TextRender()
    {
    }

    /*
     * Rendering functions
     */

    public void line(String text, float size, Vector3f position, Key style)
    {
        lines.add(TextLine.fromText(text, size, position, style));
    }

    public void centeredLine(String text, float size, Vector3f center, Key style)
    {
        Font font = VolkaniumsRegistries.FONT_STYLE.get(style).style().font();
        lines.add(TextLine.fromText(text, size, new Vector3f(center).sub(font.getWidth(text, size) / 2f, 0, 0), style));
    }

    public void line(String text, float size, Vector3f position)
    {
        lines.add(TextLine.fromText(text, size, position, FontStyle.DEFAULT));
    }

    public void centeredLine(String text, float size, Vector3f center)
    {
        centeredLine(text, size, center, FontStyle.DEFAULT);
    }

    /// Deprecated - internal
    @Deprecated
    public List<TextLine> lines()
    {
        return lines;
    }
}
