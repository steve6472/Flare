package steve6472.flare.ui.font.render;

import org.joml.Matrix4f;

/**
 * Created by steve6472
 * Date: 11/13/2024
 * Project: Flare <br>
 */
public record TextLineObject(TextLine line, long startTime, long endTime, Matrix4f transformFrom, Matrix4f transformTo)
{
}
