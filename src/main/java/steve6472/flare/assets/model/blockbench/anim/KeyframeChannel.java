package steve6472.flare.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4f;
import steve6472.core.log.Log;
import steve6472.core.util.MathUtil;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.model.blockbench.anim.datapoint.*;
import steve6472.orlang.OrlangEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public abstract class KeyframeChannel<T extends DataPoint> implements KeyFrame
{
    private static final Logger LOGGER = Log.getLogger(KeyframeChannel.class);

    private final Interpolation interpolation;
    private final double time;
    private final List<T> dataPoints;

    /**
     *
     */
    public KeyframeChannel(Interpolation interpolation, double time, List<T> dataPoints)
    {
        this.interpolation = interpolation;
        this.time = time;
        this.dataPoints = dataPoints;
    }

    @FunctionalInterface
    protected interface Constructor<T extends DataPoint, R>
    {
        R apply(Interpolation interpolation, double time, List<T> dataPoints);
    }

    protected static <T extends DataPoint, R extends KeyframeChannel<T>> Codec<R> createKeyframe(Codec<T> datapointCodec, Constructor<T, R> constructor)
    {
        return RecordCodecBuilder.create(instance -> instance.group(
                Interpolation.CODEC.fieldOf("interpolation").forGetter(KeyframeChannel::interpolation),
                Codec.DOUBLE.fieldOf("time").forGetter(KeyframeChannel::time),
                datapointCodec.listOf().fieldOf("data_points").forGetter(KeyframeChannel::dataPoints))
            .apply(instance, constructor::apply));
    }

    public Interpolation interpolation()
    {
        return interpolation;
    }

    public double time()
    {
        return time;
    }

    public List<T> dataPoints()
    {
        return dataPoints;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (KeyframeChannel) obj;
        return Objects.equals(this.interpolation, that.interpolation) && Double.doubleToLongBits(this.time) == Double.doubleToLongBits(that.time) && Objects.equals(this.dataPoints, that.dataPoints);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(interpolation, time, dataPoints);
    }

    @Override
    public String toString()
    {
        return "Keyframe[" + "interpolation=" + interpolation + ", " + "time=" + time + ", " + "dataPoints=" + dataPoints + ']';
    }

    public static abstract class EffectKeyframeChannel<T extends DataPoint> extends KeyframeChannel<T>
    {
        public EffectKeyframeChannel(Interpolation interpolation, double time, List<T> dataPoints)
        {
            super(interpolation, time, dataPoints);
        }

        public abstract void processKeyframe(T effect);
    }

    public static abstract class AnimationKeyframeChannel<T extends DataPoint> extends KeyframeChannel<T>
    {
        public AnimationKeyframeChannel(Interpolation interpolation, double time, List<T> dataPoints)
        {
            super(interpolation, time, dataPoints);
        }

        public abstract void processKeyframe(T first, T second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env);
    }


    public static final class RotationKeyframe extends AnimationKeyframeChannel<Vec3DataPoint>
    {
        public static final Codec<RotationKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, RotationKeyframe::new);
        public RotationKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.ROTATION; }

        @Override
        public void processKeyframe(Vec3DataPoint first, Vec3DataPoint second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env)
        {
            first.xyz().evaluate(env);
            second.xyz().evaluate(env);

            double x = MathUtil.lerp(first.xyz().x(), second.xyz().x(), ticks) * FlareConstants.DEG_TO_RAD;
            double y = MathUtil.lerp(first.xyz().y(), second.xyz().y(), ticks) * FlareConstants.DEG_TO_RAD;
            double z = MathUtil.lerp(first.xyz().z(), second.xyz().z(), ticks) * FlareConstants.DEG_TO_RAD;

            transform.rotateZ((float) z * (invert ? -1f : 1f));
            transform.rotateY((float) -y * (invert ? -1f : 1f));
            transform.rotateX((float) -x * (invert ? -1f : 1f));
        }
    }

    public static final class PositionKeyframe extends AnimationKeyframeChannel<Vec3DataPoint>
    {
        public static final Codec<PositionKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, PositionKeyframe::new);
        public PositionKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.POSITION; }

        @Override
        public void processKeyframe(Vec3DataPoint first, Vec3DataPoint second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env)
        {
            first.xyz().evaluate(env);
            second.xyz().evaluate(env);

            double x = MathUtil.lerp(first.xyz().x(), second.xyz().x(), ticks) * FlareConstants.BB_MODEL_SCALE;
            double y = MathUtil.lerp(first.xyz().y(), second.xyz().y(), ticks) * FlareConstants.BB_MODEL_SCALE;
            double z = MathUtil.lerp(first.xyz().z(), second.xyz().z(), ticks) * FlareConstants.BB_MODEL_SCALE;

            transform.translateLocal((float) -x * (invert ? -1f : 1f), (float) y * (invert ? -1f : 1f), (float) z * (invert ? -1f : 1f));
        }
    }

    public static final class ScaleKeyframe extends AnimationKeyframeChannel<Vec3DataPoint>
    {
        public static final Codec<ScaleKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, ScaleKeyframe::new);
        public ScaleKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.SCALE; }

        @Override
        public void processKeyframe(Vec3DataPoint first, Vec3DataPoint second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env)
        {
            first.xyz().evaluate(env);
            second.xyz().evaluate(env);

            double x = MathUtil.lerp(first.xyz().x(), second.xyz().x(), ticks);
            double y = MathUtil.lerp(first.xyz().y(), second.xyz().y(), ticks);
            double z = MathUtil.lerp(first.xyz().z(), second.xyz().z(), ticks);

            transform.scale((float) x * (invert ? -1f : 1f), (float) y * (invert ? -1f : 1f), (float) z * (invert ? -1f : 1f));
        }
    }




    public static final class ParticleKeyframe extends EffectKeyframeChannel<ParticleDataPoint>
    {
        public static final Codec<ParticleKeyframe> CODEC = createKeyframe(ParticleDataPoint.CODEC, ParticleKeyframe::new);
        public ParticleKeyframe(Interpolation interpolation, double time, List<ParticleDataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.PARTICLE; }

        @Override
        public void processKeyframe(ParticleDataPoint effect)
        {
            LOGGER.fine("Unimplemented particle: " + effect);
        }
    }

    public static final class SoundKeyframe extends EffectKeyframeChannel<SoundDataPoint>
    {
        public static final Codec<SoundKeyframe> CODEC = createKeyframe(SoundDataPoint.CODEC, SoundKeyframe::new);
        public SoundKeyframe(Interpolation interpolation, double time, List<SoundDataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.SOUND; }

        @Override
        public void processKeyframe(SoundDataPoint effect)
        {
            LOGGER.fine("Unimplemented sound: " + effect);
        }
    }

    public static final class TimelineKeyframe extends EffectKeyframeChannel<TimelineDataPoint>
    {
        public static final Codec<TimelineKeyframe> CODEC = createKeyframe(TimelineDataPoint.CODEC, TimelineKeyframe::new);
        public TimelineKeyframe(Interpolation interpolation, double time, List<TimelineDataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.TIMELINE; }

        @Override
        public void processKeyframe(TimelineDataPoint effect)
        {
            LOGGER.fine("Unimplemented timeline: " + effect);
        }
    }
}
