package steve6472.volkaniums.render.debug.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.def.Vertex;

import java.util.List;

/**
 * Created by steve6472
 * Date: 10/11/2024
 * Project: Volkaniums <br>
 */
public record Capsule(float radius, float height, int quality, Vector4f color) implements DebugObject
{
    @Override
    public void addVerticies(List<Struct> vertices, Matrix4f transform)
    {
        new Cylinder(radius, height - radius, quality, color).addVerticies(vertices, transform);
        new Hemisphere(radius, height - radius, quality, true, color).addVerticies(vertices, transform);
        new Hemisphere(radius, height - radius, quality, false, color).addVerticies(vertices, transform);
    }
}
