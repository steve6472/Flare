package steve6472.flare.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public enum Interpolation implements StringValue
{
    LINEAR("linear"),
    CATMULLROM("catmullrom"),
    BEZIER("bezier");

    public static final Codec<Interpolation> CODEC = StringValue.fromValues(Interpolation::values);

    private final String value;

    Interpolation(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
