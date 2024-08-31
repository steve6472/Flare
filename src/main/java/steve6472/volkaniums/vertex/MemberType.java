package steve6472.volkaniums.vertex;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public interface MemberType
{
    MemberData<Vector3f> VEC_3F = new MemberData<>(
        Vector3f.class,
        Vector3f::new,
        VK_FORMAT_R32G32B32_SFLOAT,
        (buff, obj) -> buff
            .putFloat(obj.x)
            .putFloat(obj.y)
            .putFloat(obj.z)
    );

    MemberData<Vector2f> VEC_2F = new MemberData<>(
        Vector2f.class,
        Vector2f::new,
        VK_FORMAT_R32G32_SFLOAT,
        (buff, obj) -> buff
            .putFloat(obj.x)
            .putFloat(obj.y)
    );

    MemberData<Float> FLOAT = new MemberData<>(
        Float.class,
        () -> 0f,
        VK_FORMAT_R32_SFLOAT,
        ByteBuffer::putFloat
    );

    /*
     * Convenience references
     */

    MemberData<Vector2f> UV = VEC_2F;
}
