package steve6472.volkaniums.render.debug.objects;

import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.struct.Struct;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/21/2024
 * Project: Volkaniums <br>
 */
public record DebugLine(Vector3f start, Vector3f end, Vector4f startColor, Vector4f endColor) implements DebugObject
{
    public DebugLine(Vector3f start, Vector3f end, Vector4f color)
    {
        this(start, end, color, color);
    }

    @Override
    public void addVerticies(List<Struct> vertices)
    {
        vertices.add(vertex(start, startColor));
        vertices.add(vertex(end, endColor));
    }

    @Override
    public int vertexCount()
    {
        return 2;
    }
}
