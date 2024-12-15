package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

import java.util.Locale;

/**
 * Created by steve6472
 * Date: 12/12/2024
 * Project: Flare <br>
 */
public enum NewLineType implements StringValue
{
    MAX_HEIGHT,
    FIXED;

    public static final Codec<NewLineType> CODEC = StringValue.fromValues(NewLineType::values);

    @Override
    public String stringValue()
    {
        return name().toLowerCase(Locale.ROOT);
    }
}
