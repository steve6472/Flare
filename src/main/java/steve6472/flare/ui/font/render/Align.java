package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

import java.util.Locale;

/**
 * Created by steve6472
 * Date: 11/13/2024
 * Project: Flare <br>
 */
public enum Align implements StringValue
{
    CENTER,
    LEFT,
    RIGHT;

    public static Codec<Align> CODEC = StringValue.fromValues(Align::values);

    @Override
    public String stringValue()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
