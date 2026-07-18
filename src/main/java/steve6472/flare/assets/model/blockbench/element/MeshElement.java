package steve6472.flare.assets.model.blockbench.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.*;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.util.ExtraCodecs;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.model.blockbench.*;
import steve6472.flare.ui.textures.SpriteEntry;

import java.lang.Math;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public record MeshElement(UUID uuid, String name, Vector3f rotation, Vector3f origin, Map<String, Vector3f> vertices, Map<String, MeshFace> faces) implements Element
{
    public static final MapCodec<MeshElement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        Codec.STRING.fieldOf("name").forGetter(o -> o.name),
        ExtraCodecs.VEC_3F.fieldOf("rotation").forGetter(o -> o.rotation),
        ExtraCodecs.VEC_3F.fieldOf("origin").forGetter(o -> o.origin),
        ExtraCodecs.mapListCodec(Codec.STRING, ExtraCodecs.VEC_3F).fieldOf("vertices").forGetter(o -> o.vertices),
        ExtraCodecs.mapListCodec(Codec.STRING, MeshFace.CODEC).fieldOf("faces").forGetter(o ->o.faces)
        ).apply(instance, (uuid1, name1, rotation1, origin1, vertices1, faces1) ->
        new MeshElement(
            uuid1,
            name1,
            rotation1.mul(FlareConstants.DEG_TO_RAD),
            origin1.mul(FlareConstants.BB_MODEL_SCALE),
            scaleVertices(vertices1),
            faces1))
    );

    private static Map<String, Vector3f> scaleVertices(Map<String, Vector3f> vertices)
    {
        vertices.forEach((_, v) -> v.mul(FlareConstants.BB_MODEL_SCALE));
        return vertices;
    }

    private float mapUV(float in, float inMax, float outMin, float outMax)
    {
        return in * (outMax - outMin) / inMax + outMin;
    }

    @Override
    public void fixUvs(LoadedModel model, Atlas atlas)
    {
        faces.forEach((_, face) -> {
            TextureData textureData = model.textures().get(face.texture());
            String textureId = textureData.name();
            Key key = Key.parse(FlareConstants.NAMESPACE, textureId);
            SpriteEntry sprite = atlas.getSprite(key);
            face.uv().forEach((_, uv) -> uv.set(
                mapUV(uv.x, textureData.uvWidth(), sprite.uv().x, sprite.uv().z),
                mapUV(uv.y, textureData.uvHeight(), sprite.uv().y, sprite.uv().w)
            ));
        });
    }

    @Override
    public List<Vector3f> toVertices()
    {
        List<Vector3f> vertexes = new ArrayList<>();

        Matrix4f modelTransform = new Matrix4f();
        modelTransform.translate(origin);
        modelTransform.rotateZ(rotation().z);
        modelTransform.rotateY(rotation().y);
        modelTransform.rotateX(rotation().x);
//        modelTransform.translate(-origin().x, -origin().y, -origin().z);

        faces.forEach((String _, MeshFace v) ->
        {
            for (String vertex : sortedVerticies(v.vertices()))
            {
                vertexes.add(new Vector3f(vertices.get(vertex)).mulPosition(modelTransform));
            }
        });

        return vertexes;
    }

    @Override
    public List<Vector3f> toNormals()
    {
        List<Vector3f> normals = new ArrayList<>(vertices.size());

        faces.forEach((String _, MeshFace v) ->
        {
            List<String> faceVerts = sortedVerticies(v.vertices());

            if (faceVerts.size() % 3 != 0)
                throw new RuntimeException("Not triangles!");

            for (int i = 0; i < faceVerts.size() / 3; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    normals.add(getNormal(faceVerts, i * 3, i * 3 + 1, i * 3 + 2));
                }
            }
        });

        return normals;
    }

    @Override
    public List<Vector2f> toUVs()
    {
        List<Vector3f> vertices1 = toVertices();
        List<Vector2f> uvs = new ArrayList<>(vertices1.size());
        faces.forEach((String _, MeshFace v) ->
        {
            for (String vertex : sortedVerticies(v.vertices()))
            {
                uvs.add(new Vector2f(v.uv().get(vertex)));
            }
        });
        return uvs;
    }

    private Vector3f getNormal(List<String> faceVerts, int i0, int i1, int i2)
    {
        return getNormal(vertices.get(faceVerts.get(i0)), vertices.get(faceVerts.get(i1)), vertices.get(faceVerts.get(i2)));
    }

    private Vector3f getNormal(Vector3f a, Vector3f b, Vector3f c)
    {
        Vector3f normal = new Vector3f();
        GeometryUtils.normal(a, b, c, normal);
        return normal;
    }

    private List<String> sortedVerticies(List<String> verts)
    {
        if (verts.size() < 4) return verts;

        Vector3f originalNormal = getNormal(verts, 0, 1, 2);
        float d0 = getNormal(verts, 0, 3, 1).dot(originalNormal);
        float d1 = getNormal(verts, 1, 3, 2).dot(originalNormal);
        float d2 = getNormal(verts, 2, 3, 0).dot(originalNormal);

        float max = Math.max(d0, Math.max(d1, d2));

        if (max == d0)
        {
            return List.of(verts.get(3), verts.get(0), verts.get(1), verts.get(0), verts.get(2), verts.get(1));
        }
        if (max == d1)
        {
            return List.of(verts.get(3), verts.get(0), verts.get(1), verts.get(3), verts.get(2), verts.get(0));
        } else
        {
            return List.of(verts.get(3), verts.get(0), verts.get(1), verts.get(2), verts.get(3), verts.get(1));
        }
    }

    @Override
    public MapCodec<? extends Element> codec()
    {
        return CODEC;
    }
}
