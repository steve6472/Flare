package steve6472.volkaniums.render.debug;

import org.joml.Matrix4f;
import steve6472.volkaniums.render.debug.objects.DebugObject;

/**
 * Created by steve6472
 * Date: 9/21/2024
 * Project: Volkaniums <br>
 */
record DebugRenderTime(DebugObject object, long untilTime, Matrix4f transform)
{
}
