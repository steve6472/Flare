package steve6472.volkaniums.input;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public enum KeybindType implements StringValue
{
    ONCE, REPEAT;

    public static final Codec<KeybindType> CODEC = StringValue.fromValues(KeybindType::values);

    @Override
    public String stringValue()
    {
        return this == ONCE ? "once" : "repeat";
    }
}
