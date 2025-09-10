package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.*;

import java.util.List;
import java.util.Objects;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public abstract class KeyframeChannel<T extends DataPoint> implements KeyFrame
{
    private final Interpolation interpolation;
    private final double time;
    private final List<T> dataPoints;

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
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        KeyframeChannel<?> that = (KeyframeChannel<?>) o;
        return Double.compare(time, that.time) == 0 && interpolation == that.interpolation && Objects.equals(dataPoints, that.dataPoints);
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
}
