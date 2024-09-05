package steve6472.volkaniums.struct.def;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.struct.MemberData;

import static org.lwjgl.vulkan.VK10.*;
import static steve6472.volkaniums.struct.MemberData.builder;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public interface MemberType
{
    MemberData<Vector4f> VEC_4F = builder(Vector4f.class)
        .constructor(Vector4f::new)
        .format(VK_FORMAT_R32G32B32A32_SFLOAT)
        .memcpy((buff, offset, obj) -> obj.get(offset, buff))
        .build();

    MemberData<Vector3f> VEC_3F = builder(Vector3f.class)
        .constructor(Vector3f::new)
        .format(VK_FORMAT_R32G32B32_SFLOAT)
        .memcpy((buff, offset, obj) -> obj.get(offset, buff))
        .build();

    MemberData<Vector2f> VEC_2F = builder(Vector2f.class)
        .constructor(Vector2f::new)
        .format(VK_FORMAT_R32G32_SFLOAT)
        .memcpy((buff, offset, obj) -> obj.get(offset, buff))
        .build();

    MemberData<Float> FLOAT = builder(Float.class)
        .constructor(() -> 0f)
        .format(VK_FORMAT_R32_SFLOAT)
        .memcpy((buff, offset, obj) -> buff.putFloat(offset, obj))
        .build();

    MemberData<Integer> INT = builder(Integer.class)
        .constructor(() -> 0)
        .format(VK_FORMAT_R32_SINT)
        .memcpy((buff, offset, obj) -> buff.putInt(offset, obj))
        .build();

    MemberData<Matrix4f> MAT_4F = builder(Matrix4f.class)
        .constructor(Matrix4f::new)
        .memcpy((buff, offset, obj) -> obj.get(offset, buff))
        .build();

    /*
     * Convenience references
     */

    MemberData<Vector2f> UV = VEC_2F;
}
