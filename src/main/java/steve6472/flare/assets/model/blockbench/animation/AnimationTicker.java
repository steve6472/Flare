package steve6472.flare.assets.model.blockbench.animation;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.flare.assets.model.blockbench.LoadedModel;
import steve6472.flare.assets.model.blockbench.SkinData;
import steve6472.flare.assets.model.blockbench.animation.ik.Ik;
import steve6472.flare.assets.model.blockbench.animation.keyframe.AnimationKeyframeChannel;
import steve6472.flare.assets.model.blockbench.animation.keyframe.EffectKeyframeChannel;
import steve6472.flare.assets.model.blockbench.animation.keyframe.KeyframeChannel;
import steve6472.flare.assets.model.blockbench.animation.keyframe.KeyframeType;
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

    private final SkinData masterSkinData;
    private final double animationLength;
    private final LoadedModel model;
    public AnimationTimer timer;
    public SkinData skinData;
    public Timeline timeline;
    private final Ik ik;

    // Null Object UUID

    public AnimationTicker(Animation animation, LoadedModel model, SkinData masterSkinData)
    {
        this.animationLength = animation.length();
        this.masterSkinData = masterSkinData;
        this.timer = new AnimationTimer();
        this.timeline = new Timeline(animation);
        this.model = model;
        ik = new Ik(model, this);
    }

    public void tick(OrlangEnvironment env)
    {
        double currentAnimationTime = timer.calculateTime(System.currentTimeMillis());

        // TODO: this should be all in timer
        if (currentAnimationTime >= animationLength) timer.setRunning(false);
        if (timer.hasEnded() && timer.isLooping()) timer.start();
        if (timer.hasEnded() && timer.isStayAtLastFrame()) currentAnimationTime = animationLength;

        skinData = masterSkinData.copy();

        for (OutlinerUUID outlinerUUID : model.outliner())
        {
            Matrix4f transform = new Matrix4f();
            recursiveAnimation(skinData, outlinerUUID, transform, currentAnimationTime, env);
        }
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
        KeyframeChannel<?> nextKeyframe = timeline.getNextKeyframe(type, currentAnimationTime, boneName);

        if (lastKeyframe == null || nextKeyframe == null)
            return;

//        System.out.println("keyframes for bone " + boneName + " type: " + type.key().id() + " at " + "%.4f ".formatted(currentAnimationTime) + lastKeyframe + " -> " + nextKeyframe);

        //noinspection rawtypes
        if (lastKeyframe instanceof AnimationKeyframeChannel lastAnimChannel)
        {
            double ticks = timeline.calculateTicks(currentAnimationTime, lastKeyframe.time(), nextKeyframe.time());
            //noinspection unchecked
            lastAnimChannel.processKeyframe(lastKeyframe.dataPoints().getFirst(), nextKeyframe.dataPoints().getFirst(), ticks, transform, invert, env);
        } else if (lastKeyframe instanceof EffectKeyframeChannel<?> lastEffectChannel)
        {
            throw new RuntimeException("Effects are not implemented yet, it's 9:37PM and my head already hurts a bit");
        } else
        {
            throw new RuntimeException("Unknown keyframe type");
        }

    }

    private void processEffect(KeyframeChannel<?> channel)
    {
        // Remember that they have to activate only once per their frame
    }
}
