package steve6472.flare.assets.model.blockbench.animation.datapoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public record SoundDataPoint(String effect, String file) implements DataPoint
{
    public static final Codec<SoundDataPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("effect").forGetter(o -> o.effect),
        Codec.STRING.fieldOf("file").forGetter(o -> o.file)
    ).apply(instance, SoundDataPoint::new));
}
