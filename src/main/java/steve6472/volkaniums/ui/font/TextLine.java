package steve6472.volkaniums.ui.font;

import org.joml.Vector3f;
import steve6472.core.registry.Key;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.ui.font.style.FontStyle;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 */
public record TextLine(Vector3f startPos, float size, char[] charEntries, FontStyleEntry style)
{
    public static TextLine fromText(String text, float size, Vector3f position, Key style)
    {
        return new TextLine(position, size, text.toCharArray(), findStyle(style));
    }

    public static TextLine fromText(String text, float size, Vector3f position)
    {
        return new TextLine(position, size, text.toCharArray(), findStyle(FontStyle.DEFAULT));
    }

    private static FontStyleEntry findStyle(Key key)
    {
        return VolkaniumsRegistries.FONT_STYLE.get(key);
    }
}
