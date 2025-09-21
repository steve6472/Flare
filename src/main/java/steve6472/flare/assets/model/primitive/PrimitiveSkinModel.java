package steve6472.flare.assets.model.primitive;

import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.type.StructVertex;

import java.util.*;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public class PrimitiveSkinModel extends PrimitiveStaticModel
{
    public List<Integer> transformationIndicies;
    public List<LocatorData> locatorNames;
    public SkinData skinData;
    LoadedModel model;

    public PrimitiveSkinModel(StructVertex vertexType, LoadedModel model)
    {
        super(vertexType);
        this.model = model;
        transformationIndicies = new ArrayList<>();
        locatorNames = new ArrayList<>();
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
            Vector3f normal = normals.get(i);
            Vector2f uv = texCoords.get(i);
            Struct vertex = vertexType.create(pos, normal, uv, transformationIndicies.get(i));
            vertices.add(vertex);
        }
        return vertices;
    }

    public record LocatorData(int transformIndex, UUID uuid, String name, Vector3f position) {}
}
