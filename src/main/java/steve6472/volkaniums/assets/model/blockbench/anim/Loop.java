package steve6472.volkaniums.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public enum Loop implements StringValue
{
    LOOP("loop"), ONCE("once");

    public static final Codec<Loop> CODEC = StringValue.fromValues(Loop::values);

    private final String value;

    Loop(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
