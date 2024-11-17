package steve6472.flare.assets.model.blockbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector4f;
import steve6472.core.util.ExtraCodecs;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public record CubeFace(Vector4f uv, int texture)
{
    public static final Codec<CubeFace> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.VEC_4F.fieldOf("uv").forGetter(o -> o.uv),
        Codec.INT.optionalFieldOf("texture", -1).forGetter(o -> o.texture)
        ).apply(instance, CubeFace::new)
    );
}
