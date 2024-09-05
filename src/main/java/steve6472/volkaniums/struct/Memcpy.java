package steve6472.volkaniums.struct;

import java.nio.ByteBuffer;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
@FunctionalInterface
public interface Memcpy<T>
{
    void accept(ByteBuffer buffer, int alignment, T data);
}
