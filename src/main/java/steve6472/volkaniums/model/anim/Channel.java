package steve6472.volkaniums.model.anim;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.registry.StringValue;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public enum Channel implements StringValue
{
    ROTATION("rotation"),
    POSITION("position"),
    SCALE("scale"),

    PARTICLE("particle"),
    SOUND("sound"),
    TIMELINE("timeline");

    public static final Codec<Channel> CODEC = StringValue.fromValues(Channel::values);

    private final String value;

    Channel(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
