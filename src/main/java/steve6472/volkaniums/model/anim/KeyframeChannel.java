package steve6472.volkaniums.model.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.volkaniums.model.anim.datapoint.*;

import java.util.List;
import java.util.Objects;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public abstract class KeyframeChannel<T extends DataPoint> implements KeyFrame
{
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

    public static final class RotationKeyframe extends KeyframeChannel<Vec3DataPoint>
    {
        public static final Codec<RotationKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, RotationKeyframe::new);
        public RotationKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.ROTATION; }
    }

    public static final class PositionKeyframe extends KeyframeChannel<Vec3DataPoint>
    {
        public static final Codec<PositionKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, PositionKeyframe::new);
        public PositionKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.POSITION; }
    }

    public static final class ScaleKeyframe extends KeyframeChannel<Vec3DataPoint>
    {
        public static final Codec<ScaleKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, ScaleKeyframe::new);
        public ScaleKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.SCALE; }
    }




    public static final class ParticleKeyframe extends KeyframeChannel<ParticleDataPoint>
    {
        public static final Codec<ParticleKeyframe> CODEC = createKeyframe(ParticleDataPoint.CODEC, ParticleKeyframe::new);
        public ParticleKeyframe(Interpolation interpolation, double time, List<ParticleDataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.PARTICLE; }
    }

    public static final class SoundKeyframe extends KeyframeChannel<SoundDataPoint>
    {
        public static final Codec<SoundKeyframe> CODEC = createKeyframe(SoundDataPoint.CODEC, SoundKeyframe::new);
        public SoundKeyframe(Interpolation interpolation, double time, List<SoundDataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.SOUND; }
    }

    public static final class TimelineKeyframe extends KeyframeChannel<TimelineDataPoint>
    {
        public static final Codec<TimelineKeyframe> CODEC = createKeyframe(TimelineDataPoint.CODEC, TimelineKeyframe::new);
        public TimelineKeyframe(Interpolation interpolation, double time, List<TimelineDataPoint> dataPoints)  { super(interpolation, time, dataPoints); }
        @Override public KeyframeType<?> getType()  { return KeyframeType.TIMELINE; }
    }
}
