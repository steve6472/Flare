package steve6472.flare.render.debug.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Vertex;

import java.util.List;

/**
 * Created by steve6472
 * Date: 10/11/2024
 * Project: Flare <br>
 */
public record DebugCylinder(float radius, float height, int quality, Vector4f color) implements DebugObject
{
    @Override
    public void addVerticies(List<Struct> vertices, Matrix4f transform)
    {
        int segments = quality + 3; // Number of segments for each circular slice

        for (int i = 0; i < segments; i++)
        {
            float theta = (float) (2.0 * Math.PI * i / segments); // Angle for current slice
            float nextTheta = (float) (2.0 * Math.PI * (i + 1) / segments); // Angle for next slice

            // Circle coordinates for the current and next vertices
            float x1 = radius * (float) Math.cos(theta);
            float z1 = radius * (float) Math.sin(theta);
            float x2 = radius * (float) Math.cos(nextTheta);
            float z2 = radius * (float) Math.sin(nextTheta);

            // Create vertical lines for the cylindrical part
            Vector3f top1 = new Vector3f(x1, height, z1).mulPosition(transform);
            Vector3f bottom1 = new Vector3f(x1, -height, z1).mulPosition(transform);
            Vector3f top2 = new Vector3f(x2, height, z2).mulPosition(transform);
            Vector3f bottom2 = new Vector3f(x2, -height, z2).mulPosition(transform);

            // Add vertical lines
            vertices.add(vertex(top1, color));
            vertices.add(vertex(bottom1, color));

            // Add horizontal lines connecting circles
            vertices.add(vertex(top1, color));
            vertices.add(vertex(top2, color));
            vertices.add(vertex(bottom1, color));
            vertices.add(vertex(bottom2, color));
        }
    }
}
