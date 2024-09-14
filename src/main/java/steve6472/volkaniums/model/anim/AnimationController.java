package steve6472.volkaniums.model.anim;

import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.model.LoadedModel;
import steve6472.volkaniums.model.OutlinerElement;
import steve6472.volkaniums.model.OutlinerUUID;
import steve6472.volkaniums.model.SkinData;
import steve6472.volkaniums.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/10/2024
 * Project: Volkaniums <br>
 */
public class AnimationController
{
    private static final Logger LOGGER = Log.getLogger(AnimationController.class);

    private static final String EFFECTS = "effects";

    private final SkinData masterSkinData;
    private final double animationLength;
    private final LoadedModel model;
    public AnimationTimer timer;
    public SkinData skinData;
    public Timeline timeline;

    public AnimationController(Animation animation, SkinData skinData, LoadedModel model)
    {
        this.animationLength = animation.length();
        this.masterSkinData = skinData.copy();
        this.timer = new AnimationTimer();
        this.timeline = new Timeline(animation);
        this.model = model;
    }

    public void tick()
    {
        double currentAnimationTime = timer.calculateTime(System.currentTimeMillis());

        // TODO: this should be all in timer
        if (currentAnimationTime >= animationLength) timer.setRunning(false);
        if (timer.hasEnded() && timer.isLooping()) timer.start();
        if (timer.hasEnded() && timer.isStayAtLastFrame()) currentAnimationTime = animationLength;

        skinData = masterSkinData.copy();

//        LOGGER.finest("last: %.4f edit: %.4f stopped: %s".formatted(lastCurAdwdawd, currentAnimationTime, stopped));

        for (OutlinerUUID outlinerUUID : model.outliner())
        {
            Matrix4f transform = new Matrix4f();
            recursiveAnimation(skinData, outlinerUUID, transform, currentAnimationTime);
        }
    }

    private void animateBone(String boneName, KeyframeType<?> type, double currentAnimationTime, Matrix4f transform)
    {
        KeyframeChannel<?> lastKeyframe = timeline.getLastKeyframe(type, currentAnimationTime, boneName);
        KeyframeChannel<?> nextKeyframe = timeline.getNextKeyframe(type, currentAnimationTime, boneName);

        if (lastKeyframe == null || nextKeyframe == null)
            return;

        if (!(lastKeyframe instanceof KeyframeChannel.AnimationKeyframeChannel lastAnimChannel))
            throw new RuntimeException("Unknown keyframe type");

        double ticks = timeline.calculateTicks(currentAnimationTime, lastKeyframe.time(), nextKeyframe.time());
        lastAnimChannel.processKeyframe(lastKeyframe.dataPoints().getFirst(), nextKeyframe.dataPoints().getFirst(), ticks, transform);
    }

    private void recursiveAnimation(SkinData skinData, OutlinerUUID parent, Matrix4f transform, double animTime)
    {
        if (parent instanceof OutlinerElement outEl)
        {
            String boneName = parent.uuid().toString();
            Matrix4f newTransform = new Matrix4f(transform);

            newTransform.translate(outEl.origin());
            animateBone(boneName, KeyframeType.POSITION, animTime, newTransform);
            animateBone(boneName, KeyframeType.ROTATION, animTime, newTransform);
            animateBone(boneName, KeyframeType.SCALE, animTime, newTransform);
            newTransform.translate(-outEl.origin().x, -outEl.origin().y, -outEl.origin().z);

            for (OutlinerUUID child : outEl.children())
            {
                recursiveAnimation(skinData, child, newTransform, animTime);
            }

            skinData.transformations.get(parent.uuid()).getSecond().mul(newTransform);
        }
    }

    private void processEffect(KeyframeChannel<?> channel)
    {
        // Remember that they have to activate only once per their frame
    }
}
