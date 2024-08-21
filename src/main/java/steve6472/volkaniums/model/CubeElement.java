package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.util.ExtraCodecs;

import java.util.*;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record CubeElement(UUID uuid, Vector3f from, Vector3f to, Vector3f origin, Map<FaceType, CubeFace> faces) implements Element
{
    public static final Codec<CubeElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        ExtraCodecs.VEC_3F.fieldOf("from").forGetter(o -> o.from),
        ExtraCodecs.VEC_3F.fieldOf("to").forGetter(o -> o.to),
        ExtraCodecs.VEC_3F.fieldOf("origin").forGetter(o -> o.origin),
        ExtraCodecs.mapListCodec(FaceType.CODEC, CubeFace.CODEC).fieldOf("faces").forGetter(o -> o.faces)
        ).apply(instance, (uuid1, from1, to1, origin1, faces1) ->
        {
            Map<FaceType, CubeFace> newFaces = new HashMap<>();
            faces1.forEach((k, v) -> {
                if (!(v.uv().x == 0 && v.uv().y == 0 && v.uv().z == 0 && v.uv().w == 0))
                {
                    newFaces.put(k, v);
                }
            });
            return new CubeElement(uuid1, from1, to1, origin1, newFaces);
        })
    );

    @Override
    public ElementType<?> getType()
    {
        return ElementType.CUBE;
    }

    @Override
    public List<Vector3f> toVertices() {
        List<Vector3f> vertices = new ArrayList<>();

        // Define the 8 vertices of the cuboid using 'from' and 'to'
//        Vector3f v000 = new Vector3f(from);
//        Vector3f v001 = new Vector3f(from.x, from.y, to.z);
//        Vector3f v010 = new Vector3f(from.x, to.y, from.z);
//        Vector3f v011 = new Vector3f(from.x, to.y, to.z);
//        Vector3f v100 = new Vector3f(to.x, from.y, from.z);
//        Vector3f v101 = new Vector3f(to.x, from.y, to.z);
//        Vector3f v110 = new Vector3f(to.x, to.y, from.z);
//        Vector3f v111 = new Vector3f(to.x, to.y, to.z);

        Vector3f v111 = new Vector3f(from);
        Vector3f v110 = new Vector3f(from.x, from.y, to.z);
        Vector3f v101 = new Vector3f(from.x, to.y, from.z);
        Vector3f v100 = new Vector3f(from.x, to.y, to.z);
        Vector3f v011 = new Vector3f(to.x, from.y, from.z);
        Vector3f v010 = new Vector3f(to.x, from.y, to.z);
        Vector3f v001 = new Vector3f(to.x, to.y, from.z);
        Vector3f v000 = new Vector3f(to.x, to.y, to.z);

        // Add vertices for each face if it exists
        if (faces.containsKey(FaceType.NORTH))
        {
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v010));
        }

        if (faces.containsKey(FaceType.SOUTH))
        {
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v101));
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v111));
        }

        if (faces.containsKey(FaceType.WEST))
        {
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v011));
        }

        if (faces.containsKey(FaceType.EAST))
        {
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v101));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v110));
        }

        if (faces.containsKey(FaceType.DOWN))
        {
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v101));
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v101));
        }

        if (faces.containsKey(FaceType.UP))
        {
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v011));
        }

        return vertices;
    }

    @Override
    public List<Vector2f> toUVs()
    {
        List<Vector2f> uvCoords = new ArrayList<>();

        for (FaceType face : FaceType.values())
        {
            if (faces.containsKey(face))
            {
                Vector4f uv = faces.get(face).uv();

                // Top-left (minU, maxV), Top-right (maxU, maxV)
                // Bottom-left (minU, minV), Bottom-right (maxU, minV)
                uvCoords.add(new Vector2f(uv.x, uv.w)); // Top-left
                uvCoords.add(new Vector2f(uv.z, uv.w)); // Top-right
                uvCoords.add(new Vector2f(uv.z, uv.y)); // Bottom-right
                uvCoords.add(new Vector2f(uv.x, uv.w)); // Top-left
                uvCoords.add(new Vector2f(uv.z, uv.y)); // Bottom-right
                uvCoords.add(new Vector2f(uv.x, uv.y)); // Bottom-left

                // Skip over the next 6 vertices since we've already processed them
            }
        }

        return uvCoords;
    }
}
