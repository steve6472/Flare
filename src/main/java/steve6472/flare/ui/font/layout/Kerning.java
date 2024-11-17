package steve6472.flare.ui.font.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public record Kerning(int unicode1, int unicode2, float advance)
{
    public static final Codec<Kerning> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("unicode1").forGetter(Kerning::unicode1),
        Codec.INT.fieldOf("unicode2").forGetter(Kerning::unicode2),
        Codec.FLOAT.fieldOf("advance").forGetter(Kerning::advance)
    ).apply(instance, Kerning::new));

    public static final Kerning EMPTY = new Kerning(0, 0, 0f);
}
