package steve6472.volkaniums.ui.font.render;

import com.mojang.serialization.Codec;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import steve6472.core.registry.StringValue;
import steve6472.volkaniums.Camera;

import java.util.Locale;

/**
 * Created by steve6472
 * Date: 11/13/2024
 * Project: Volkaniums <br>
 */
public enum Billboard implements StringValue
{
    FIXED,
    FACE_VERTICAL,
    FACE_HORIZONTAL,
    FACE_CENTER;

    public static final Codec<Billboard> CODEC = StringValue.fromValues(Billboard::values);

    public void apply(Camera camera, Matrix4f transform)
    {
        switch (this)
        {
            case FACE_CENTER ->
            {
                Matrix4f invert = new Matrix4f(camera.getViewMatrix()).invert();
                Matrix3f rotMat = new Matrix3f(invert);

                transform.mul(new Matrix4f(rotMat));
            }
            case FACE_HORIZONTAL ->
            {
                Matrix4f invert = new Matrix4f(camera.getViewMatrix()).invert();
                Matrix3f rotMat = new Matrix3f(invert);

                rotMat.m00 = 1.0f;
                rotMat.m01 = 0.0f;
                rotMat.m02 = 0.0f;
                rotMat.m20 = 0.0f;
                rotMat.m21 = 0.0f;
                rotMat.m22 = 1.0f;

                transform.mul(new Matrix4f(rotMat));
            }
            case FACE_VERTICAL ->
            {
                Matrix4f invert = new Matrix4f(camera.getViewMatrix()).invert();
                Matrix3f rotMat = new Matrix3f(invert);

                rotMat.m10 = 0.0f;
                rotMat.m11 = 1.0f;
                rotMat.m12 = 0.0f;

                transform.mul(new Matrix4f(rotMat));
            }
        }
    }

    @Override
    public String stringValue()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
