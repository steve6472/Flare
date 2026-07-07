package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.MapCodec;
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

    public static final MapCodec<ParticleKeyframe> CODEC = createKeyframe(ParticleDataPoint.CODEC, ParticleKeyframe::new);

    public ParticleKeyframe(Interpolation interpolation, double time, List<ParticleDataPoint> dataPoints)
    {
        super(KeyframeType.PARTICLE, interpolation, time, dataPoints);
    }

    @Override
    public void processKeyframe(ParticleDataPoint effect)
    {
        LOGGER.fine("Unimplemented particle: " + effect);
    }

    @Override
    public MapCodec<? extends KeyFrame> codec()
    {
        return CODEC;
    }
}
