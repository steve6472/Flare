package steve6472.volkaniums.assets.model.blockbench;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.registry.StringValue;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public enum FaceType implements StringValue
{
    NORTH("north"),
    EAST("east"),
    SOUTH("south"),
    WEST("west"),
    UP("up"),
    DOWN("down");

    public static final Codec<FaceType> CODEC = StringValue.fromValues(FaceType::values);

    private final String value;

    FaceType(String value)
    {
        this.value = value;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
