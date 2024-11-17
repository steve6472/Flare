package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Flare <br>
 */
public record Metrics(int emSize, float lineHeight, float ascender, float descender, float underlineY, float underlineThickness)
{
    public static final Codec<Metrics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("emSize").forGetter(Metrics::emSize),
        Codec.FLOAT.fieldOf("lineHeight").forGetter(Metrics::lineHeight),
        Codec.FLOAT.fieldOf("ascender").forGetter(Metrics::ascender),
        Codec.FLOAT.fieldOf("descender").forGetter(Metrics::descender),
        Codec.FLOAT.fieldOf("underlineY").forGetter(Metrics::underlineY),
        Codec.FLOAT.fieldOf("underlineThickness").forGetter(Metrics::underlineThickness)
    ).apply(instance, Metrics::new));
}
