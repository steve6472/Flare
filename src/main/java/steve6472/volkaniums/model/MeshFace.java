package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector4f;
import steve6472.volkaniums.util.ExtraCodecs;

import java.util.List;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record MeshFace(int texture, List<String> vertices, Map<String, Vector2f> uv)
{
    public static final Codec<MeshFace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("texture").forGetter(o -> o.texture),
        Codec.STRING.listOf().fieldOf("vertices").forGetter(o -> o.vertices),
        ExtraCodecs.mapListCodec(Codec.STRING, ExtraCodecs.VEC_2F).fieldOf("uv").forGetter(o -> o.uv)
        ).apply(instance, MeshFace::new)
    );
}
