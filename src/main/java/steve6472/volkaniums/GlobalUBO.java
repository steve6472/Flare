package steve6472.volkaniums;

import org.joml.Matrix4f;

import java.nio.ByteBuffer;
import java.util.function.BiConsumer;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Volkaniums <br>
 */
public class GlobalUBO
{
    public static final int SIZEOF = 4 * Float.BYTES * 4 * 2;
    public static final int PROJECTION_OFFSET = 0;
    public static final int VIEW_OFFSET = 4 * Float.BYTES * 4;

    public static BiConsumer<ByteBuffer, GlobalUBO[]> MEMCPY = (buff, ubos) -> {
        for (GlobalUBO ubo : ubos)
        {
            ubo.projection.get(PROJECTION_OFFSET, buff);
            ubo.view.get(VIEW_OFFSET, buff);
        }
    };

    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f view = new Matrix4f();
}
