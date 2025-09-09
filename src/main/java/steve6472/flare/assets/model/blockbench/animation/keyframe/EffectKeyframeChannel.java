package steve6472.flare.assets.model.blockbench.animation.keyframe;

import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.DataPoint;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public abstract class EffectKeyframeChannel<T extends DataPoint> extends KeyframeChannel<T>
{
    public EffectKeyframeChannel(Interpolation interpolation, double time, List<T> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    public abstract void processKeyframe(T effect);
}
