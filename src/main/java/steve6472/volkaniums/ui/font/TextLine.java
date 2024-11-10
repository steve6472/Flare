package steve6472.volkaniums.ui.font;

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 */
public record TextLine(Vector3f startPos, float size, CharEntry[] charEntries)
{
    public static TextLine fromText(String text, float size, Vector3f position, Vector4f color)
    {
        CharEntry[] entries = new CharEntry[text.length()];

        char[] charArray = text.toCharArray();
        for (int i = 0; i < charArray.length; i++)
        {
            entries[i] = new CharEntry(color, charArray[i]);
        }

        return new TextLine(position, size, entries);
    }
}
