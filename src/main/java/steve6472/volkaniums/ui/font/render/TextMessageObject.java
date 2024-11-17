package steve6472.volkaniums.ui.font.render;

import org.joml.Matrix4f;

/**
 * Created by steve6472
 * Date: 11/13/2024
 * Project: Volkaniums <br>
 */
public record TextMessageObject(TextMessage message, long startTime, long endTime, Matrix4f transformFrom, Matrix4f transformTo)
{
}
