package steve6472.flare.assets.model.blockbench.animation.controller;

import steve6472.flare.assets.model.blockbench.animation.datapoint.ParticleDataPoint;
import steve6472.flare.assets.model.blockbench.animation.datapoint.SoundDataPoint;
import steve6472.flare.assets.model.blockbench.animation.datapoint.TimelineDataPoint;

import java.util.function.Consumer;

/**
 * Created by steve6472
 * Date: 9/10/2025
 * Project: Flare <br>
 */
public class AnimationCallbacks
{
    public Consumer<ParticleDataPoint> onParticle;
    public Consumer<TimelineDataPoint> onScript;
    public Consumer<SoundDataPoint> onSound;

    public Consumer<Controller> onAnimationEnd;
}
