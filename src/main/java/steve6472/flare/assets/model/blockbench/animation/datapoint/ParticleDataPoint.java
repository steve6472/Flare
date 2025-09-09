package steve6472.flare.assets.model.blockbench.animation.datapoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public record ParticleDataPoint(String effect, String locator, String script, String file) implements DataPoint
{
    public static final Codec<ParticleDataPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("effect").forGetter(o -> o.effect),
        Codec.STRING.fieldOf("locator").forGetter(o -> o.locator),
        Codec.STRING.fieldOf("script").forGetter(o -> o.script),
        Codec.STRING.fieldOf("file").forGetter(o -> o.file)
    ).apply(instance, ParticleDataPoint::new));
}
