package steve6472.volkaniums.ui.font.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Volkaniums <br>
 */
public record GlyphInfo(int index, float advance, GlyphBounds planeBounds, GlyphBounds atlasBounds)
{
    public static final Codec<GlyphInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("unicode").forGetter(GlyphInfo::index),
        Codec.FLOAT.fieldOf("advance").forGetter(GlyphInfo::advance),
        GlyphBounds.CODEC.optionalFieldOf("planeBounds", GlyphBounds.EMPTY).forGetter(GlyphInfo::planeBounds),
        GlyphBounds.CODEC.optionalFieldOf("atlasBounds", GlyphBounds.EMPTY).forGetter(GlyphInfo::atlasBounds)
    ).apply(instance, GlyphInfo::new));

    public boolean isInvisible()
    {
        return planeBounds == GlyphBounds.EMPTY || atlasBounds == GlyphBounds.EMPTY;
    }
}
