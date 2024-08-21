package steve6472.volkaniums;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import steve6472.volkaniums.util.MathUtil;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public class Camera
{
    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;

    public Camera()
    {
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
    }

    public void updateViewMatrix()
    {

    }

    public Matrix4f getViewMatrix()
    {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix()
    {
        return projectionMatrix;
    }

    public void setOrthographicProjection(float left, float right, float top, float bottom, float near, float far)
    {
        projectionMatrix.identity().ortho(left, right, bottom, top, near, far);
    }

    public void setViewDirection(Vector3f position, Vector3f direction)
    {
        viewMatrix.lookAt(direction, position, Constants.UP);
    }

    public void setVeiwTarget(Vector3f position, Vector3f target)
    {
        setViewDirection(position, target.sub(position, new Vector3f()));
    }

    public void setViewYXZ(Vector3f position, Vector3f rotation)
    {
        viewMatrix.setRotationYXZ(rotation.y, rotation.x, rotation.z);
        viewMatrix.translate(position);
    }

    /**
     *
     * @param fov fov in angle
     * @param aspect calculated as width / height
     * @param near near
     * @param far far
     */
    public void setPerspectiveProjection(float fov, float aspect, float near, float far)
    {
        projectionMatrix.identity().perspective((float) Math.toRadians(fov), aspect, near, far);
    }
}
