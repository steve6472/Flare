package steve6472.flare.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Created by steve6472
 * Date: 12/14/2024
 * Project: Flare <br>
 */
public final class FloatUtil
{
    @FunctionalInterface
    public interface ToFloatFunction<T>
    {
        float applyAsFloat(T value);
    }

    public static <T> Collector<T, float[], Float> summing(ToFloatFunction<? super T> mapper)
    {
        return new Collector<>()
        {
            @Override
            public Supplier<float[]> supplier()
            {
                return () -> new float[1];
            }

            @Override
            public BiConsumer<float[], T> accumulator()
            {
                return (f, t) -> f[0] += mapper.applyAsFloat(t);
            }

            @Override
            public BinaryOperator<float[]> combiner()
            {
                return (a, b) -> {a[0] += b[0]; return a;};
            }

            @Override
            public Function<float[], Float> finisher()
            {
                return (a) -> a[0];
            }

            @Override
            public Set<Characteristics> characteristics()
            {
                return Set.of();
            }
        };
    }

    public static<T> Comparator<T> comparingFloat(ToFloatFunction<? super T> keyExtractor)
    {
        Objects.requireNonNull(keyExtractor);
        return (c1, c2) -> Float.compare(keyExtractor.applyAsFloat(c1), keyExtractor.applyAsFloat(c2));
    }
}
