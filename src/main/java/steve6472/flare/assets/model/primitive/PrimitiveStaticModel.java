package steve6472.flare.assets.model.primitive;

import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.type.StructVertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public class PrimitiveStaticModel implements PrimitiveModel
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

    @Override
    public StructVertex vertexType()
    {
        return vertexType;
    }

    @Override
    public List<Struct> createVerticies()
    {
        checkDataSizeEqual(positions, normals, texCoords);

        List<Struct> vertices = new ArrayList<>(positions.size());

        for (int i = 0; i < positions.size(); i++)
        {
            Vector3f pos = positions.get(i);
            Vector3f normal = normals.get(i);
            Vector2f uv = texCoords.get(i);
            Struct vertex = vertexType.create(pos, normal, uv);
            vertices.add(vertex);
        }
        return vertices;
    }
}
