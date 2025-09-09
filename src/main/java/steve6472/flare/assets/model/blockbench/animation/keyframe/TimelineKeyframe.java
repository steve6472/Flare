package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import steve6472.core.log.Log;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.TimelineDataPoint;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class TimelineKeyframe extends EffectKeyframeChannel<TimelineDataPoint>
{
    private static final Logger LOGGER = Log.getLogger(TimelineKeyframe.class);

    public static final Codec<TimelineKeyframe> CODEC = createKeyframe(TimelineDataPoint.CODEC, TimelineKeyframe::new);

    public TimelineKeyframe(Interpolation interpolation, double time, List<TimelineDataPoint> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    @Override
    public KeyframeType<?> getType()
    {
        return KeyframeType.TIMELINE;
    }

    @Override
    public void processKeyframe(TimelineDataPoint effect)
    {
        LOGGER.fine("Unimplemented timeline: " + effect);
    }
}
