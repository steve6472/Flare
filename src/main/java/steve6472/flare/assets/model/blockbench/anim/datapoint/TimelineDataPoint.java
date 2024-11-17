package steve6472.flare.assets.model.blockbench.anim.datapoint;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public record TimelineDataPoint(String script) implements DataPoint
{
    public static final Codec<TimelineDataPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("script").forGetter(o -> o.script)
    ).apply(instance, TimelineDataPoint::new));
}
