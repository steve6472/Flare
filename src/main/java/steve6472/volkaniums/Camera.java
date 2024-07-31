package steve6472.volkaniums;

import org.joml.Matrix4f;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public class Camera
{
    private final Matrix4f viewMatrix;

    public Camera()
    {
        this.viewMatrix = new Matrix4f();
    }

    public void updateViewMatrix()
    {

    }

    public Matrix4f getViewMatrix()
    {
        return viewMatrix;
    }
}
