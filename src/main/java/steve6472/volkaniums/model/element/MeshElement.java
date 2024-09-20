package steve6472.volkaniums.model.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.GeometryUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.model.Element;
import steve6472.volkaniums.model.ElementType;
import steve6472.volkaniums.model.MeshFace;
import steve6472.volkaniums.util.ExtraCodecs;

import java.util.*;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record MeshElement(UUID uuid, Vector3f rotation, Vector3f origin, Map<String, Vector3f> vertices, Map<String, MeshFace> faces) implements Element
{
    public static final Codec<MeshElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        ExtraCodecs.VEC_3F.fieldOf("rotation").forGetter(o -> o.rotation),
        ExtraCodecs.VEC_3F.fieldOf("origin").forGetter(o -> o.origin),
        ExtraCodecs.mapListCodec(Codec.STRING, ExtraCodecs.VEC_3F).fieldOf("vertices").forGetter(o -> o.vertices),
        ExtraCodecs.mapListCodec(Codec.STRING, MeshFace.CODEC).fieldOf("faces").forGetter(o ->o.faces)
        ).apply(instance, (uuid1, rotation1, origin1, vertices1, faces1) -> new MeshElement(uuid1, rotation1, origin1, scaleVertices(vertices1), faces1))
    );

    private static Map<String, Vector3f> scaleVertices(Map<String, Vector3f> vertices)
    {
        vertices.forEach((k, v) -> v.mul(Constants.BB_MODEL_SCALE));
        return vertices;
    }

    @Override
    public ElementType<?> getType()
    {
        return ElementType.MESH;
    }

    @Override
    public List<Vector3f> toVertices()
    {
        List<Vector3f> vertexes = new ArrayList<>();

        faces.forEach((String k, MeshFace v) ->
        {
            List<String> faceVert = v.vertices();
            if (faceVert.size() == 3)
            {
                for (String vertex : faceVert)
                {
                    vertexes.add(new Vector3f(vertices.get(vertex)));
                }
            } else if (faceVert.size() == 4)
            {
                vertexes.add(new Vector3f(vertices.get(faceVert.get(0))));
                vertexes.add(new Vector3f(vertices.get(faceVert.get(1))));
                vertexes.add(new Vector3f(vertices.get(faceVert.get(2))));

                Vector3f originalNormal = getNormal(faceVert, 0, 1, 2);

                float d0 = getNormal(faceVert, 0, 3, 1).dot(originalNormal);
                float d1 = getNormal(faceVert, 1, 3, 2).dot(originalNormal);
                float d2 = getNormal(faceVert, 2, 3, 0).dot(originalNormal);

                float max = Math.max(d0, Math.max(d1, d2));

                if (max == d0)
                {
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(0))));
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(3))));
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(1))));
                }
                if (max == d1)
                {
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(1))));
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(3))));
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(2))));
                } else
                {
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(2))));
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(3))));
                    vertexes.add(new Vector3f(vertices.get(faceVert.get(0))));
                }
            }
        });

        return vertexes;
    }

    Vector3f getNormal(List<String> faceVerts, int i0, int i1, int i2)
    {
        return getNormal(vertices.get(faceVerts.get(i0)), vertices.get(faceVerts.get(i1)), vertices.get(faceVerts.get(i2)));
    }

    Vector3f getNormal(Vector3f a, Vector3f b, Vector3f c)
    {
        Vector3f normal = new Vector3f();
        GeometryUtils.normal(a, b, c, normal);
        return normal;
    }

    @Override
    public List<Vector2f> toUVs()
    {
        List<Vector3f> vertices1 = toVertices();
        List<Vector2f> uvs = new ArrayList<>(vertices1.size());
        for (int i = 0; i < vertices1.size(); i++)
        {
            uvs.add(new Vector2f());
        }
        return uvs;
    }
}
