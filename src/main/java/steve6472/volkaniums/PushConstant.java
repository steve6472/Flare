package steve6472.volkaniums;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import steve6472.volkaniums.util.MathUtil;

import java.nio.ByteBuffer;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Volkaniums <br>
 */
public class PushConstant
{
    public static final int SIZEOF = 4 * Float.BYTES * 4;
    public final Matrix4f transformation = new Matrix4f();

    public ByteBuffer createBuffer(MemoryStack stack)
    {
        ByteBuffer buff = stack.calloc(SIZEOF);
        //            camera.getProjectionMatrix().get(buff);
        transformation
            .identity()
            .translate(0, 0.75f, 0)
            //                .rotateY((float) Math.sin(MathUtil.animateRadians(8d)) / 2f)
            .rotateY((float) MathUtil.animateRadians(4d))
            .rotateZ((float) Math.toRadians(180))
            .scale(0.05f);

        transformation.get(0, buff);

        return buff;
    }
}
