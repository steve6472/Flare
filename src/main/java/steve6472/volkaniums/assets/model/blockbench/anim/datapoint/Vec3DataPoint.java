package steve6472.volkaniums.assets.model.blockbench.anim.datapoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.volkaniums.assets.model.blockbench.anim.ScriptValue;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public record Vec3DataPoint(ScriptValue x, ScriptValue y, ScriptValue z) implements DataPoint
{
    public static final Codec<Vec3DataPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ScriptValue.CODEC.fieldOf("x").forGetter(o -> o.x),
        ScriptValue.CODEC.fieldOf("y").forGetter(o -> o.y),
        ScriptValue.CODEC.fieldOf("z").forGetter(o -> o.z)
    ).apply(instance, Vec3DataPoint::new));

    public static Codec<Vec3DataPoint> scaledResultCodec(double scale)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
            ScriptValue.scaledResultCodec(scale).fieldOf("x").forGetter(o -> o.x),
            ScriptValue.scaledResultCodec(scale).fieldOf("y").forGetter(o -> o.y),
            ScriptValue.scaledResultCodec(scale).fieldOf("z").forGetter(o -> o.z)
        ).apply(instance, Vec3DataPoint::new));
    }
}
