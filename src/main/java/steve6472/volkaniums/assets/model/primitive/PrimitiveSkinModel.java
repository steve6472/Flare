package steve6472.volkaniums.assets.model.primitive;

import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.assets.model.blockbench.SkinData;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.type.StructVertex;

import java.util.*;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public class PrimitiveSkinModel extends PrimitiveStaticModel
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

    @Override
    public List<Struct> createVerticies()
    {
        checkDataSizeEqual(positions, normals, texCoords, transformationIndicies);

        List<Struct> vertices = new ArrayList<>(positions.size());

        for (int i = 0; i < positions.size(); i++)
        {
            Vector3f pos = positions.get(i);
            Vector3f normal = positions.get(i);
            Vector2f uv = texCoords.get(i);
            Struct vertex = vertexType.create(pos, normal, uv, transformationIndicies.get(i));
            vertices.add(vertex);
        }
        return vertices;
    }
}
