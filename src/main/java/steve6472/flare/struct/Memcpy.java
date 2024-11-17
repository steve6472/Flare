package steve6472.flare.struct;

import java.nio.ByteBuffer;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
@FunctionalInterface
public interface Memcpy<T>
{
    void accept(ByteBuffer buffer, int offset, T data);
}
