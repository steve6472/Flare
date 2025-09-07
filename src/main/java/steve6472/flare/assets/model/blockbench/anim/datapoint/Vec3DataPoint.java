package steve6472.flare.assets.model.blockbench.anim.datapoint;

import com.mojang.serialization.Codec;
import steve6472.orlang.codec.OrVec3;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public record Vec3DataPoint(OrVec3 xyz) implements DataPoint
{
    public static final Codec<Vec3DataPoint> CODEC = OrVec3.CODEC.xmap(Vec3DataPoint::new, Vec3DataPoint::xyz);


}
