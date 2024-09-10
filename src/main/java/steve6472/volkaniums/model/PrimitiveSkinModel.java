package steve6472.volkaniums.model;

import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.type.StructVertex;

import java.util.*;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public class PrimitiveSkinModel extends PrimitiveModel
{
    public List<Integer> transformationIndicies;
    public SkinData skinData;
    LoadedModel model;

    public PrimitiveSkinModel(StructVertex vertexType, LoadedModel model)
    {
        super(vertexType);
        this.model = model;
        transformationIndicies = new ArrayList<>();
        skinData = new SkinData();
    }

    @Deprecated(forRemoval = true)
    public List<Struct> toVkVertices(float uvScale)
    {
        if (positions.size() != texCoords.size())
            throw new RuntimeException("Different count of vertices and texture coordinates");

        List<Struct> vertices = new ArrayList<>(positions.size());

        for (int i = 0; i < positions.size(); i++)
        {
            Vector2f uv = texCoords.get(i);
            Vector3f pos = positions.get(i);
            Struct vertex = vertexType.create(pos, transformationIndicies.get(i), uv.mul(uvScale, new Vector2f()));
            vertices.add(vertex);
        }
        return vertices;
    }
}
