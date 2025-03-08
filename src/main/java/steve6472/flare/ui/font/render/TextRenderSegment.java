package steve6472.flare.ui.font.render;

import java.lang.reflect.Field;

/**
 * Created by steve6472
 * Date: 12/13/2024
 * Project: Flare <br>
 */
public class TextRenderSegment
{
    public int start, end;
    public float width, fontHeight;
    /// These two seem useless for text rendering
    public float minDescent, maxDescent;
    public float minAscender;
    public float maxLineHeight;


    // Yes, chatGPT wrote this, I was too lazy and quite tired lul
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append(" { ");
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            field.setAccessible(true);
            try
            {
                sb.append(field.getName()).append(": ").append(field.get(this)).append(", ");
            } catch (IllegalAccessException e)
            {
                sb.append(field.getName()).append(": [access denied], ");
            }
        }
        if (fields.length > 0)
        {
            sb.setLength(sb.length() - 2); // Remove the last comma and space
        }
        sb.append(" }");
        return sb.toString();
    }
}
