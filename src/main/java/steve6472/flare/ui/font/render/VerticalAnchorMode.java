package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import steve6472.core.registry.StringValue;

import java.util.Locale;

/**
 * Created by steve6472
 * Date: 3/8/2025
 * Project: Flare <br>
 */
public enum VerticalAnchorMode implements StringValue
{
    TEXT_HEIGHT,
    MAX_HEIGHT;

    public static Codec<VerticalAnchorMode> CODEC = StringValue.fromValues(VerticalAnchorMode::values);

    @Override
    public String stringValue()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
