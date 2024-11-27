package steve6472.flare;

import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Flare <br>
 */
public class Camera
{
    private final Matrix4f viewMatrix;
    private final Matrix4f projectionMatrix;
    public int cameraIndex;

    private float near, far;

    // TODO: single Camera UBO

    public Camera()
    {
        this.viewMatrix = new Matrix4f();
        this.projectionMatrix = new Matrix4f();
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
        this.near = near;
        this.far = far;
    }

    public void setViewDirection(Vector3f cameraPosition, Vector3f pointInSpace)
    {
        viewMatrix.identity().lookAt(pointInSpace, cameraPosition, FlareConstants.CAMERA_UP);
    }

    public void setViewTarget(Vector3f cameraPosition, Vector3f pointInSpace)
    {
        setViewDirection(pointInSpace, cameraPosition.sub(pointInSpace, new Vector3f()));
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
        this.near = near;
        this.far = far;
    }

    public float near()
    {
        return near;
    }

    public float far()
    {
        return far;
    }

    public Vector3f viewPosition = new Vector3f(), center = new Vector3f();
    protected float yaw;
    protected float pitch;
    public float roll;

    public int oldx;
    public int oldy;

    public float yaw()
    {
        return yaw;
    }

    public float pitch()
    {
        return pitch;
    }

    public void head(int mouseX, int mouseY, float sensitivity)
    {
        if (mouseX != oldx)
        {
            int dx = oldx - mouseX;
            yaw += (float) Math.toRadians((float) dx * sensitivity);
        }

        if (mouseY != oldy)
        {
            int dy = oldy - mouseY;
            pitch += (float) Math.toRadians((float) dy * sensitivity);
        }

        oldx = mouseX;
        oldy = mouseY;

        if (pitch > 1.5707963267948966f) pitch = 1.5707963267948966f;
        if (pitch < -1.5707963267948966f) pitch = -1.5707963267948966f;

        while (yaw > 6.283185307179586f) yaw -= 6.283185307179586f;
        while (yaw < 0) yaw += 6.283185307179586f;
    }

    public void updateViewMatrix()
    {
        viewMatrix.identity();

        viewMatrix.rotate(pitch(), -1, 0, 0);
        viewMatrix.rotate(yaw(), 0, -1, 0);
        viewMatrix.rotate(roll, 0, 0, -1);

        viewMatrix.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);
    }

    /**
     *
     * @param mouseX mouseX
     * @param mouseY mouseY
     * @param sensitivity sensitivity
     * @param orbitalDistance distance from orbited point
     */
    public void headOrbit(int mouseX, int mouseY, float sensitivity, float orbitalDistance)
    {
        head(mouseX, mouseY, sensitivity);
        calculateOrbit(orbitalDistance);

        viewMatrix.identity();

        viewMatrix.rotate(pitch, -1, 0, 0);
        viewMatrix.rotate(yaw, 0, -1, 0);

        viewMatrix.translate(-viewPosition.x, -viewPosition.y, -viewPosition.z);
    }

    public void calculateOrbit(float orbitalDistance)
    {
        float x, y, z;
        x = (float) (Math.sin(yaw) * (Math.cos(pitch) * orbitalDistance)) + center.x;
        y = (float) (-Math.sin(pitch) * orbitalDistance) + center.y;
        z = (float) (Math.cos(yaw) * (Math.cos(pitch) * orbitalDistance)) + center.z;
        viewPosition.set(x, y, z);
    }
}
