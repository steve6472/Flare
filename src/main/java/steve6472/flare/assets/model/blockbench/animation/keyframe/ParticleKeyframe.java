package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import steve6472.core.log.Log;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.ParticleDataPoint;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class ParticleKeyframe extends EffectKeyframeChannel<ParticleDataPoint>
{
    private static final Logger LOGGER = Log.getLogger(ParticleKeyframe.class);

    public static final Codec<ParticleKeyframe> CODEC = createKeyframe(ParticleDataPoint.CODEC, ParticleKeyframe::new);

    public ParticleKeyframe(Interpolation interpolation, double time, List<ParticleDataPoint> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    @Override
    public KeyframeType<?> getType()
    {
        return KeyframeType.PARTICLE;
    }

    @Override
    public void processKeyframe(ParticleDataPoint effect)
    {
        LOGGER.fine("Unimplemented particle: " + effect);
    }
}
