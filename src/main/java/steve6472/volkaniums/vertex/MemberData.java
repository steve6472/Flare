package steve6472.volkaniums.vertex;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public record MemberData<T>(Class<T> clazz, Supplier<T> constructor, int format, BiConsumer<ByteBuffer, T> memcpy)
{
}
