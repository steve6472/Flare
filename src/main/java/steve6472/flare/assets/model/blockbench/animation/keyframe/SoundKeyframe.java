package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import steve6472.core.log.Log;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.SoundDataPoint;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class SoundKeyframe extends EffectKeyframeChannel<SoundDataPoint>
{
    private static final Logger LOGGER = Log.getLogger(SoundKeyframe.class);

    public static final Codec<SoundKeyframe> CODEC = createKeyframe(SoundDataPoint.CODEC, SoundKeyframe::new);

    public SoundKeyframe(Interpolation interpolation, double time, List<SoundDataPoint> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    @Override
    public KeyframeType<?> getType()
    {
        return KeyframeType.SOUND;
    }

    @Override
    public void processKeyframe(SoundDataPoint effect)
    {
        LOGGER.fine("Unimplemented sound: " + effect);
    }
}
