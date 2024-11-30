package steve6472.flare.render.debug.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Vertex;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/21/2024
 * Project: Flare <br>
 */
public record DebugCuboid(Vector3f start, Vector3f end, Vector4f color) implements DebugObject
{
    public DebugCuboid(Vector3f center, float halfSize, Vector4f color)
    {
        this(new Vector3f(center).sub(halfSize, halfSize, halfSize), new Vector3f(center).add(halfSize, halfSize, halfSize), color);
    }

    public DebugCuboid(Vector3f center, float halfWidth, float halfHeight, float halfDepth, Vector4f color)
    {
        this(new Vector3f(center).sub(halfWidth, halfHeight, halfDepth), new Vector3f(center).add(halfWidth, halfHeight, halfDepth), color);
    }

    @Override
    public void addVerticies(List<Struct> vertices, Matrix4f transform)
    {
        // The 8 corners of the cube are calculated from the start and end vectors
        Vector3f p0 = new Vector3f(start.x, start.y, start.z);
        Vector3f p1 = new Vector3f(end.x, start.y, start.z);
        Vector3f p2 = new Vector3f(end.x, end.y, start.z);
        Vector3f p3 = new Vector3f(start.x, end.y, start.z);

        Vector3f p4 = new Vector3f(start.x, start.y, end.z);
        Vector3f p5 = new Vector3f(end.x, start.y, end.z);
        Vector3f p6 = new Vector3f(end.x, end.y, end.z);
        Vector3f p7 = new Vector3f(start.x, end.y, end.z);

        p0.mulPosition(transform);
        p1.mulPosition(transform);
        p2.mulPosition(transform);
        p3.mulPosition(transform);
        p4.mulPosition(transform);
        p5.mulPosition(transform);
        p6.mulPosition(transform);
        p7.mulPosition(transform);

        // Front face (p0, p1, p2, p3)
        vertices.add(vertex(p0, color));
        vertices.add(vertex(p1, color));

        vertices.add(vertex(p1, color));
        vertices.add(vertex(p2, color));

        vertices.add(vertex(p2, color));
        vertices.add(vertex(p3, color));

        vertices.add(vertex(p3, color));
        vertices.add(vertex(p0, color));

        // Back face (p4, p5, p6, p7)
        vertices.add(vertex(p4, color));
        vertices.add(vertex(p5, color));

        vertices.add(vertex(p5, color));
        vertices.add(vertex(p6, color));

        vertices.add(vertex(p6, color));
        vertices.add(vertex(p7, color));

        vertices.add(vertex(p7, color));
        vertices.add(vertex(p4, color));

        // Connect front and back faces
        vertices.add(vertex(p0, color));
        vertices.add(vertex(p4, color));

        vertices.add(vertex(p1, color));
        vertices.add(vertex(p5, color));

        vertices.add(vertex(p2, color));
        vertices.add(vertex(p6, color));

        vertices.add(vertex(p3, color));
        vertices.add(vertex(p7, color));
    }
}
