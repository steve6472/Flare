package steve6472.volkaniums.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3d;
import steve6472.volkaniums.util.ExtraCodecs;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public record BezierData(boolean linked, Vector3d leftTime, Vector3d leftValue, Vector3d rightTime, Vector3d rightValue)
{
    public static final Codec<BezierData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("linked").forGetter(o -> o.linked),
        ExtraCodecs.VEC_3D.fieldOf("bezier_left_time").forGetter(o -> o.leftTime),
        ExtraCodecs.VEC_3D.fieldOf("bezier_left_value").forGetter(o -> o.leftValue),
        ExtraCodecs.VEC_3D.fieldOf("bezier_right_time").forGetter(o -> o.rightTime),
        ExtraCodecs.VEC_3D.fieldOf("bezier_right_value").forGetter(o -> o.rightValue)
    ).apply(instance, BezierData::new));
}
