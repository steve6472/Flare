package steve6472.volkaniums.ui.font;

import org.joml.Vector3f;
import steve6472.core.registry.Key;
import steve6472.volkaniums.ui.font.style.FontStyle;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 */
public record TextLine(Vector3f startPos, float size, char[] charEntries, Key style)
{
    public static TextLine fromText(String text, float size, Vector3f position, Key style)
    {
        return new TextLine(position, size, text.toCharArray(), style);
    }

    public static TextLine fromText(String text, float size, Vector3f position)
    {
        return new TextLine(position, size, text.toCharArray(), FontStyle.BASE_KEY);
    }
}
