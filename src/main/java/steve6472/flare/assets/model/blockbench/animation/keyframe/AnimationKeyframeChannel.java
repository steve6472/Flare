package steve6472.flare.assets.model.blockbench.animation.keyframe;

import org.joml.Matrix4f;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.DataPoint;
import steve6472.orlang.OrlangEnvironment;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public abstract class AnimationKeyframeChannel<T extends DataPoint> extends KeyframeChannel<T>
{
    public AnimationKeyframeChannel(Interpolation interpolation, double time, List<T> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    public abstract void processKeyframe(T first, T second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env);
}
