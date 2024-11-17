package steve6472.flare.render.debug.objects;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import steve6472.flare.struct.Struct;

import java.util.List;

/**
 * Created by steve6472
 * Date: 10/11/2024
 * Project: Flare <br>
 */
public record DebugSphere(float radius, int quality, Vector4f color) implements DebugObject
{
    @Override
    public void addVerticies(List<Struct> vertices, Matrix4f transform)
    {
        new DebugHemisphere(radius, 0, quality, true, color).addVerticies(vertices, transform);
        new DebugHemisphere(radius, 0, quality, false, color).addVerticies(vertices, transform);
    }
}
