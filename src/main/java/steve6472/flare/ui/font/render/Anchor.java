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
public enum Anchor implements StringValue
{
    CENTER((o, w, t, b) -> o.set(-w / 2f, (t + b) / 2f)),
    LEFT((o, _, t, b) -> o.set(0, (t + b) / 2f)),
    RIGHT((o, w, t, b) -> o.set(-w, (t + b) / 2f)),
    TOP((o, w, t, _) -> o.set(-w / 2f, t)),
    BOTTOM((o, w, _, b) -> o.set(-w / 2f, b)),
    TOP_LEFT((o, _, t, _) -> o.set(0, t)),
    TOP_RIGHT((o, w, t, _) -> o.set(-w, t)),
    BOTTOM_LEFT((o, _, _, b) -> o.set(0, b)),
    BOTTOM_RIGHT((o, w, _, b) -> o.set(-w, b));

    public static final Codec<Anchor> CODEC = StringValue.fromValues(Anchor::values);

    private final AnchorOffset offset;

    Anchor(AnchorOffset offset)
    {
        this.offset = offset;
    }

    public void applyOffset(Vector2f offset, float width, float top, float bottom)
    {
        this.offset.apply(offset, width, top, bottom);
    }

    @Override
    public String stringValue()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
