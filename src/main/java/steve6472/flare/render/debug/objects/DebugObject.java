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
public interface DebugObject
{
    void addVerticies(List<Struct> vertices, Matrix4f transform);

    default Struct vertex(Vector3f vec, Vector4f col)
    {
        return Vertex.POS3F_COL4F.create(vec, col);
    }
}
