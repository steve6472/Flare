package steve6472.volkaniums.assets.model.blockbench.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.assets.model.blockbench.*;
import steve6472.volkaniums.util.ExtraCodecs;
import steve6472.volkaniums.util.ImagePacker;
import steve6472.volkaniums.util.Preconditions;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record CubeElement(UUID uuid, Vector3f from, Vector3f to, Vector3f origin, float inflate, Map<FaceType, CubeFace> faces) implements Element
{
    public static final Codec<CubeElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        ExtraCodecs.VEC_3F.fieldOf("from").forGetter(o -> o.from),
        ExtraCodecs.VEC_3F.fieldOf("to").forGetter(o -> o.to),
        ExtraCodecs.VEC_3F.fieldOf("origin").forGetter(o -> o.origin),
        Codec.FLOAT.optionalFieldOf("inflate", 0.0f).forGetter(o -> o.inflate),
        ExtraCodecs.mapListCodec(FaceType.CODEC, CubeFace.CODEC).fieldOf("faces").forGetter(o -> o.faces)
        ).apply(instance, (uuid1, from1, to1, origin1, inflate1, faces1) ->
        {
            Map<FaceType, CubeFace> newFaces = new HashMap<>();
            faces1.forEach((k, v) -> {
                if (v.texture() != -1 && !(v.uv().x == 0 && v.uv().y == 0 && v.uv().z == 0 && v.uv().w == 0))
                {
                    newFaces.put(k, v);
                }
            });
            return new CubeElement(uuid1, from1.mul(Constants.BB_MODEL_SCALE), to1.mul(Constants.BB_MODEL_SCALE), origin1, inflate1 * Constants.BB_MODEL_SCALE, newFaces);
        })
    );

    @Override
    public ElementType<?> getType()
    {
        return ElementType.CUBE;
    }

    @Override
    public void fixUvs(LoadedModel model, ImagePacker packer)
    {
        float resX = 1f / model.resolution().width();
        float resY = 1f / model.resolution().height();
        float texel = 1f / packer.getImage().getWidth();

        faces.forEach((_, face) -> {
            TextureData textureData = model.textures().get(face.texture());
            String textureId = textureData.relativePath();
            Rectangle rectangle = packer.getRects().get(textureId);
            Preconditions.checkNotNull(rectangle, "Texture data not found in ImagePacker, for " + textureId);
            Vector4f uv = face.uv();
            uv.set(
                (rectangle.x + rectangle.width * uv.x * resX) * texel,
                (rectangle.y + rectangle.height * uv.y * resY) * texel,
                (rectangle.x + rectangle.width * uv.z * resX) * texel,
                (rectangle.y + rectangle.height * uv.w * resY) * texel
            );
        });
    }

    @Override
    public List<Vector3f> toVertices() {
        List<Vector3f> vertices = new ArrayList<>();

        // Define the 8 vertices of the cuboid using 'from' and 'to'

        Vector3f v111 = new Vector3f(from).add(-inflate, -inflate, -inflate);
        Vector3f v110 = new Vector3f(from.x, from.y, to.z).add(-inflate, -inflate, inflate);
        Vector3f v101 = new Vector3f(from.x, to.y, from.z).add(-inflate, inflate, -inflate);
        Vector3f v100 = new Vector3f(from.x, to.y, to.z).add(-inflate, inflate, inflate);
        Vector3f v011 = new Vector3f(to.x, from.y, from.z).add(inflate, -inflate, -inflate);
        Vector3f v010 = new Vector3f(to.x, from.y, to.z).add(inflate, -inflate, inflate);
        Vector3f v001 = new Vector3f(to.x, to.y, from.z).add(inflate, inflate, -inflate);
        Vector3f v000 = new Vector3f(to.x, to.y, to.z).add(inflate, inflate, inflate);

        // Add vertices for each face if it exists
        if (faces.containsKey(FaceType.NORTH))
        {
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v101));
            vertices.add(new Vector3f(v001));
        }

        if (faces.containsKey(FaceType.EAST))
        {
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v000));
        }

        if (faces.containsKey(FaceType.SOUTH))
        {
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v100));
        }

        if (faces.containsKey(FaceType.WEST))
        {
            vertices.add(new Vector3f(v101));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v101));
        }

        if (faces.containsKey(FaceType.UP))
        {
            vertices.add(new Vector3f(v101));
            vertices.add(new Vector3f(v100));
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v000));
            vertices.add(new Vector3f(v001));
            vertices.add(new Vector3f(v101));
        }

        if (faces.containsKey(FaceType.DOWN))
        {
            vertices.add(new Vector3f(v110));
            vertices.add(new Vector3f(v111));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v011));
            vertices.add(new Vector3f(v010));
            vertices.add(new Vector3f(v110));
        }

        return vertices;
    }

    @Override
    public List<Vector3f> toNormals()
    {
        List<Vector3f> normals = new ArrayList<>();
        if (faces.containsKey(FaceType.NORTH)) addNormalForFace(normals, new Vector3f(0, 0, -1));
        if (faces.containsKey(FaceType.EAST)) addNormalForFace(normals, new Vector3f(1, 0, 0));
        if (faces.containsKey(FaceType.SOUTH)) addNormalForFace(normals, new Vector3f(0, 0, 1));
        if (faces.containsKey(FaceType.WEST)) addNormalForFace(normals, new Vector3f(-1, 0, 0));
        if (faces.containsKey(FaceType.UP)) addNormalForFace(normals, new Vector3f(0, 1, 0));
        if (faces.containsKey(FaceType.DOWN)) addNormalForFace(normals, new Vector3f(0, -1, 0));
        return normals;
    }

    private void addNormalForFace(List<Vector3f> normals, Vector3f normal)
    {
        for (int i = 0; i < 6; i++)
        {
            normals.add(normal);
        }
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
                Vector2f tl = new Vector2f(uv.x, uv.y);
                Vector2f br = new Vector2f(uv.z, uv.w);

                uvCoords.add(new Vector2f(tl.x, tl.y));
                uvCoords.add(new Vector2f(tl.x, br.y));
                uvCoords.add(new Vector2f(br.x, br.y));
                uvCoords.add(new Vector2f(br.x, br.y));
                uvCoords.add(new Vector2f(br.x, tl.y));
                uvCoords.add(new Vector2f(tl.x, tl.y));
            }
        }

        return uvCoords;
    }
}
