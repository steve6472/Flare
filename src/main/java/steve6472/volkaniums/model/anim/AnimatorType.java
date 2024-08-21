package steve6472.volkaniums.model.anim;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.registry.StringValue;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public enum AnimatorType implements StringValue
{
    BONE("bone"),
    EFFECT("effect");

    public static final Codec<AnimatorType> CODEC = StringValue.fromValues(AnimatorType::values);

    private final String value;

    AnimatorType(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
