package steve6472.flare.assets.model.blockbench.anim;

import java.util.*;

/**
 * Created by steve6472
 * Date: 9/11/2024
 * Project: Flare <br>
 */
public class Timeline
{
    private final Map<String, Map<KeyframeType<?>, TreeMap<Double, KeyframeChannel<?>>>> timeline = new HashMap<>();
    private final Set<String> boneNames;

    public Timeline(Animation animation)
    {
        createTimeline(animation);
        boneNames = Collections.unmodifiableSet(timeline.keySet());
        System.out.println(timeline);
    }

    private void createTimeline(Animation animation)
    {
        animation.animators().forEach((boneName, animator) ->
        {
            for (KeyFrame keyframe : animator.keyframes())
            {
                KeyframeType<?> type = keyframe.getType();
                Map<KeyframeType<?>, TreeMap<Double, KeyframeChannel<?>>> bone = timeline.computeIfAbsent(boneName, (key) -> new HashMap<>());
                TreeMap<Double, KeyframeChannel<?>> boneByType = bone.computeIfAbsent(type, (t) -> new TreeMap<>());

                if (KeyframeChannel.class.isAssignableFrom(keyframe.getClass()))
                    boneByType.put(keyframe.time(), (KeyframeChannel<?>) keyframe);
                else
                    throw new RuntimeException("Unknown keyframe type, " + keyframe.getClass().getSimpleName());
            }
        });
    }

    public KeyframeChannel<?> getNextKeyframe(KeyframeType<?> type, double time, String boneName)
    {
        var keyframesForBone = timeline.get(boneName);
        if (keyframesForBone == null) return null;
        var keyframesByType = keyframesForBone.get(type);
        if (keyframesByType == null) return null;

        for (double v : keyframesByType.keySet())
        {
            if (v == time || v > time)
                return keyframesByType.get(v);
        }

        return keyframesByType.lastEntry().getValue();
    }

    public KeyframeChannel<?> getLastKeyframe(KeyframeType<?> type, double time, String boneName)
    {
        var keyframesForBone = timeline.get(boneName);
        if (keyframesForBone == null) return null;
        var keyframesByType = keyframesForBone.get(type);
        if (keyframesByType == null) return null;

        double lastTime = 0;

        for (double v : keyframesByType.keySet())
        {
            if (v == time || v > time)
                return keyframesByType.get(lastTime);

            lastTime = v;
        }

        return keyframesByType.lastEntry().getValue();
    }

    public double calculateTicks(double currentAnimTime, double lastTime, double nextTime)
    {
        if (lastTime == nextTime)
            return 1.0;

        return (currentAnimTime - lastTime) / (nextTime - lastTime);
    }

    public Set<String> getBoneNames()
    {
        return boneNames;
    }

    @Override
    public String toString()
    {
        return "Timeline{" + "timeline=" + timeline + '}';
    }
}
