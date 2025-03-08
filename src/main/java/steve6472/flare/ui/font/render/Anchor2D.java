package steve6472.flare.ui.font.render;

import com.mojang.serialization.Codec;
import org.joml.Vector2f;
import steve6472.core.registry.StringValue;

import java.util.Locale;

/**
 * Created by steve6472
 * Date: 11/13/2024
 * Project: Flare <br>
 */
public enum Anchor2D implements StringValue
{
    TOP_LEFT,
    MIDDLE_LEFT,
    BOTTOM_LEFT,

    TOP_CENTER,
    MIDDLE_CENTER,
    BOTTOM_CENTER,

    TOP_RIGHT,
    MIDDLE_RIGHT,
    BOTTOM_RIGHT,

    BASELINE_LEFT(true),
    BASELINE_CENTER(true),
    BASELINE_RIGHT(true);

    public static final Codec<Anchor2D> CODEC = StringValue.fromValues(Anchor2D::values);
    public final boolean singleLine;

    Anchor2D()
    {
        singleLine = false;
    }

    Anchor2D(boolean singleLine)
    {
        this.singleLine = singleLine;
    }

    @Override
    public String stringValue()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
