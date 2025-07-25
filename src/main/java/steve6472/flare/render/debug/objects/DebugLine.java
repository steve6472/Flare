package steve6472.flare.render.debug.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.flare.struct.Struct;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/21/2024
 * Project: Flare <br>
 */
public record DebugLine(Vector3f start, Vector3f end, Vector4f startColor, Vector4f endColor) implements DebugObject
{
    public DebugLine(Vector3f start, Vector3f end, Vector4f color)
    {
        this(start, end, color, color);
    }

    @Override
    public void addVerticies(List<Struct> vertices, Matrix4f transform)
    {
        vertices.add(vertex(start, startColor));
        vertices.add(vertex(end, endColor));
    }
}
