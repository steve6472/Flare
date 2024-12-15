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
    CENTER((o, w, _, b) -> o.set(-w / 2f, -b / 2f)),
    LEFT((o, _, t, b) -> o.set(0, (t + b) / 2f)),
    RIGHT((o, w, t, b) -> o.set(-w, (t + b) / 2f)),
    TOP((o, w, _, b) -> o.set(-w / 2f, b)),
    BOTTOM((o, w, t, _) -> o.set(-w / 2f, t)),
    TOP_LEFT((o, _, _, _) -> o.set(0, 0)),
    TOP_RIGHT((o, w, _, b) -> o.set(-w, b)),
    BOTTOM_LEFT((o, _, t, b) -> o.set(0, b)),
    BOTTOM_RIGHT((o, w, t, _) -> o.set(-w, t));

    public static final Codec<Anchor2D> CODEC = StringValue.fromValues(Anchor2D::values);

    private final AnchorOffset offset;

    Anchor2D(AnchorOffset offset)
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
