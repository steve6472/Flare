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
public record DebugCross(Vector3f center, float halfSize, Vector4f colorX, Vector4f colorY, Vector4f colorZ) implements DebugObject
{
    public DebugCross(Vector3f center, float halfSize, Vector4f color)
    {
        this(center, halfSize, color, color, color);
    }

    @Override
    public void addVerticies(List<Struct> vertices)
    {
        // X
        vertices.add(vertex(new Vector3f(center).add(halfSize, 0, 0), colorX));
        vertices.add(vertex(new Vector3f(center).sub(halfSize, 0, 0), colorX));
        // Y
        vertices.add(vertex(new Vector3f(center).add(0, halfSize, 0), colorY));
        vertices.add(vertex(new Vector3f(center).sub(0, halfSize, 0), colorY));

        // Z
        vertices.add(vertex(new Vector3f(center).add(0, 0, halfSize), colorZ));
        vertices.add(vertex(new Vector3f(center).sub(0, 0, halfSize), colorZ));
    }

    @Override
    public int vertexCount()
    {
        return 6;
    }
}
