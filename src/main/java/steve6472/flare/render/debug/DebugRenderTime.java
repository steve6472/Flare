package steve6472.flare.render.debug;

import org.joml.Matrix4f;
import steve6472.flare.render.debug.objects.DebugObject;

/**
 * Created by steve6472
 * Date: 9/21/2024
 * Project: Flare <br>
 */
record DebugRenderTime(DebugObject object, long startTime, long endTime, Matrix4f transformFrom, Matrix4f transformTo)
{
}
