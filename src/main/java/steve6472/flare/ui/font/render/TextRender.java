package steve6472.flare.ui.font.render;

import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 */
public class TextRender
{
    private final List<TextLineObject> lines = new ArrayList<>(256);
    private final List<TextMessageObject> messages = new ArrayList<>(256);
    private static final Matrix4f IDENTITY = new Matrix4f().identity();

    public TextRender()
    {
    }

    /*
     * Rendering functions - Line
     */

    public void line(TextLine line)
    {
        lines.add(new TextLineObject(line, 0, 0, IDENTITY, IDENTITY));
    }

    public void line(TextLine line, Matrix4f transform)
    {
        lines.add(new TextLineObject(line, 0, 0, transform, transform));
    }

    public void timedLine(TextLine line, long aliveMs)
    {
        lines.add(new TextLineObject(line, System.currentTimeMillis(), System.currentTimeMillis() + aliveMs, IDENTITY, IDENTITY));
    }

    public void timedLine(TextLine line, long aliveMs, Matrix4f transformFrom)
    {
        lines.add(new TextLineObject(line, System.currentTimeMillis(), System.currentTimeMillis() + aliveMs, transformFrom, transformFrom));
    }

    public void timedLine(TextLine line, long aliveMs, Matrix4f transformFrom, Matrix4f transformTo)
    {
        lines.add(new TextLineObject(line, System.currentTimeMillis(), System.currentTimeMillis() + aliveMs, transformFrom, transformTo));
    }

    /*
     * Rendering functions - Message
     */

    public void message(TextMessage message)
    {
        messages.add(new TextMessageObject(message, 0, 0, IDENTITY, IDENTITY));
    }

    /// Deprecated - internal
    @Deprecated
    public List<TextLineObject> lines()
    {
        return lines;
    }

    /// Deprecated - internal
    @Deprecated
    public List<TextMessageObject> messages()
    {
        return messages;
    }
}
