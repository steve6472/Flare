package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public record GlyphBounds(float left, float bottom, float right, float top)
{
    public static final Codec<GlyphBounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("left").forGetter(GlyphBounds::left),
        Codec.FLOAT.fieldOf("bottom").forGetter(GlyphBounds::bottom),
        Codec.FLOAT.fieldOf("right").forGetter(GlyphBounds::right),
        Codec.FLOAT.fieldOf("top").forGetter(GlyphBounds::top)
    ).apply(instance, GlyphBounds::new));

    public static final GlyphBounds EMPTY = new GlyphBounds(0, 0, 0, 0);

    public float width()
    {
        return Math.abs(left - right);
    }

    public float height()
    {
        return Math.abs(top - bottom);
    }
}
