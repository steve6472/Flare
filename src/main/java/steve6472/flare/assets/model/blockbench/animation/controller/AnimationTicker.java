package steve6472.flare.assets.model.blockbench.animation.controller;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.assets.model.blockbench.animation.Animation;
import steve6472.flare.assets.model.blockbench.animation.AnimationTimer;
import steve6472.flare.assets.model.blockbench.animation.Timeline;
import steve6472.flare.assets.model.blockbench.animation.datapoint.ParticleDataPoint;
import steve6472.flare.assets.model.blockbench.animation.datapoint.SoundDataPoint;
import steve6472.flare.assets.model.blockbench.animation.datapoint.TimelineDataPoint;
import steve6472.flare.assets.model.blockbench.animation.ik.Ik;
import steve6472.flare.assets.model.blockbench.animation.keyframe.*;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerElement;
import steve6472.flare.assets.model.blockbench.outliner.OutlinerUUID;
import steve6472.orlang.OrlangEnvironment;

import static steve6472.flare.render.debug.DebugRender.*;

/**
 * Created by steve6472
 * Date: 9/10/2024
 * Project: Flare <br>
 */
public class AnimationTicker
{
    public static boolean ENABLE_DEBUG_RENDER = false;
    private static final String EFFECTS_NAME = "effects";

    private final AnimationController controller;
    private final double animationLength;
    private final LoadedModel model;
    public AnimationTimer timer;
    public SkinData skinData;
    public Timeline timeline;
    private final Ik ik;

    private double lastParticleTime = -1;
    private double lastTimelineTime = -1;
    private double lastSoundTime = -1;

    // Null Object UUID

    public AnimationTicker(Animation animation, AnimationController controller)
    {
        this.controller = controller;
        this.animationLength = animation.length();
        this.timer = new AnimationTimer();
        this.timeline = new Timeline(animation);
        this.model = controller.model;
        ik = new Ik(model, this);
    }

    public void tick(OrlangEnvironment env, Controller controller)
    {
        double currentAnimationTime = timer.calculateTime(System.currentTimeMillis());

        if (currentAnimationTime >= animationLength)
        {
            timer.setRunning(false);
        }

        if (timer.hasEnded() && timer.isLooping())
        {
            timer.start();
            lastParticleTime = -1;
            lastTimelineTime = -1;
            lastSoundTime = -1;
        }

        if (timer.hasEnded() && timer.isStayAtLastFrame())
        {
            currentAnimationTime = animationLength;
        }

        if (timer.hasEnded())
        {
            if (this.controller.callbacks.onAnimationEnd != null)
            {
                this.controller.callbacks.onAnimationEnd.accept(controller);
            }
        }

        skinData = this.controller.masterSkinData.copy();

        for (OutlinerUUID outlinerUUID : model.outliner())
        {
            Matrix4f transform = new Matrix4f();
            recursiveAnimation(skinData, outlinerUUID, transform, currentAnimationTime, env);
        }

        lastParticleTime = animateEffects(KeyframeType.PARTICLE, currentAnimationTime, lastParticleTime);
        lastTimelineTime = animateEffects(KeyframeType.TIMELINE, currentAnimationTime, lastTimelineTime);
        lastSoundTime = animateEffects(KeyframeType.SOUND, currentAnimationTime, lastSoundTime);
    }

    private void recursiveAnimation(SkinData skinData, OutlinerUUID parent, Matrix4f transform, double animTime, OrlangEnvironment env)
    {
        if (parent instanceof OutlinerElement outEl)
        {
            String boneName = parent.uuid().toString();
            Matrix4f newTransform = new Matrix4f(transform);

            newTransform.translate(outEl.origin());
            animateBone(boneName, KeyframeType.POSITION, animTime, newTransform, false, env);
            animateBone(boneName, KeyframeType.ROTATION, animTime, newTransform, false, env);
            animateBone(boneName, KeyframeType.SCALE, animTime, newTransform, false, env);
            newTransform.translate(-outEl.origin().x, -outEl.origin().y, -outEl.origin().z);

            Vector3f translation = new Vector3f(outEl.origin());
            newTransform.transformPosition(translation);

            for (OutlinerUUID child : outEl.children())
            {
                recursiveAnimation(skinData, child, newTransform, animTime, env);

                if (child instanceof OutlinerElement outElChild)
                {
                    Vector3f translation_ = new Vector3f(outElChild.origin());
                    transform.transformPosition(translation_);
                    if (ENABLE_DEBUG_RENDER)
                        addDebugObjectForFrame(line(translation_, translation, RED));
                } else if (child instanceof OutlinerUUID outlinerUUID)
                {
                    ik.tick(outlinerUUID, transform, animTime, skinData, env);
                }
            }

            if (ENABLE_DEBUG_RENDER)
                addDebugObjectForFrame(lineCube(translation, 0.1f, PURPLE));

            skinData.transformations.get(parent.uuid()).getSecond().mul(newTransform);
        }
    }

    // TODO: some way to programmatically set the end effector for specific IK

    public void animateBone(String boneName, KeyframeType<?> type, double currentAnimationTime, Matrix4f transform, boolean invert, OrlangEnvironment env)
    {
        KeyframeChannel<?> lastKeyframe = timeline.getLastKeyframe(type, currentAnimationTime, boneName);

        if (lastKeyframe == null)
            return;

        //noinspection rawtypes
        if (lastKeyframe instanceof AnimationKeyframeChannel lastAnimChannel)
        {
            KeyframeChannel<?> nextKeyframe = timeline.getNextKeyframe(type, currentAnimationTime, boneName);
            if (nextKeyframe == null)
                return;

            double ticks = timeline.calculateTicks(currentAnimationTime, lastKeyframe.time(), nextKeyframe.time());
            //noinspection unchecked
            lastAnimChannel.processKeyframe(lastKeyframe.dataPoints().getFirst(), nextKeyframe.dataPoints().getFirst(), ticks, transform, invert, env);
        } else
        {
            throw new RuntimeException("Unknown keyframe type");
        }
    }

    public double animateEffects(KeyframeType<?> type, double currentAnimationTime, double testTime)
    {
        KeyframeChannel<?> lastKeyframe = timeline.getLastKeyframe(type, currentAnimationTime, EFFECTS_NAME);

        if (lastKeyframe == null)
            return testTime;

        if (lastKeyframe instanceof EffectKeyframeChannel<?> lastEffectChannel)
        {
            if (lastKeyframe.time() == testTime)
                return testTime;

            processEffect(lastEffectChannel);
            return lastKeyframe.time();
        } else
        {
            throw new RuntimeException("Unknown keyframe type");
        }
    }

    private void processEffect(EffectKeyframeChannel<?> channel)
    {
        switch (channel)
        {
            case ParticleKeyframe particle when controller.callbacks.onParticle != null ->
            {
                for (ParticleDataPoint dataPoint : particle.dataPoints())
                {
                    controller.callbacks.onParticle.accept(dataPoint);
                }
            }
            case SoundKeyframe sound when controller.callbacks.onParticle != null ->
            {
                for (SoundDataPoint dataPoint : sound.dataPoints())
                {
                    controller.callbacks.onSound.accept(dataPoint);
                }
            }
            case TimelineKeyframe script when controller.callbacks.onParticle != null ->
            {
                for (TimelineDataPoint dataPoint : script.dataPoints())
                {
                    controller.callbacks.onScript.accept(dataPoint);
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + channel);
        }
    }
}
