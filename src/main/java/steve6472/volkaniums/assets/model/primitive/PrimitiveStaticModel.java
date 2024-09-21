package steve6472.volkaniums.assets.model.primitive;

import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.type.StructVertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public class PrimitiveStaticModel
{
    public final List<Vector3f> positions;
    public final List<Vector3f> normals;
    public final List<Vector2f> texCoords;
    public final StructVertex vertexType;

    public PrimitiveStaticModel(StructVertex vertexType)
    {
        this.positions = new ArrayList<>();
        this.normals = new ArrayList<>();
        this.texCoords = new ArrayList<>();
        this.vertexType = vertexType;
    }

    @Deprecated(forRemoval = true)
    public List<Struct> toVkVertices(float uvScale)
    {
        if (positions.size() != texCoords.size() || positions.size() != normals.size())
            throw new RuntimeException("Different count of vertices, normals and texture coordinates (" + positions.size() + ", " + normals.size() + ", " + texCoords.size() + ")");

        List<Struct> vertices = new ArrayList<>(positions.size());

        for (int i = 0; i < positions.size(); i++)
        {
            Vector3f pos = positions.get(i);
            Vector3f normal = normals.get(i);
            Vector2f uv = texCoords.get(i);
            Struct vertex = vertexType.create(pos, normal, uv.mul(uvScale, new Vector2f()));
            vertices.add(vertex);
        }
        return vertices;
    }
}
