package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public record FontMetrics(AtlasData atlas, Metrics metrics, List<GlyphInfo> glyphs, List<Kerning> kernings)
{
    public static final Codec<FontMetrics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        AtlasData.CODEC.fieldOf("atlas").forGetter(FontMetrics::atlas),
        Metrics.CODEC.fieldOf("metrics").forGetter(FontMetrics::metrics),
        GlyphInfo.CODEC.listOf().fieldOf("glyphs").forGetter(FontMetrics::glyphs),
        Kerning.CODEC.listOf().optionalFieldOf("kerning", List.of()).forGetter(FontMetrics::kernings)
    ).apply(instance, FontMetrics::new));
}
