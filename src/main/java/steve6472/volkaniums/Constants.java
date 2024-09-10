package steve6472.volkaniums;

import org.joml.Vector3f;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Volkaniums <br>
 */
public class Constants
{
    public static final Vector3f CAMERA_UP = new Vector3f(0, 1, 0);

    /**
     * This makes models be unable to be saved unless they are scaled in opposite direction before saving
     */
    public static final float BB_MODEL_SCALE = 1f / 16f;

    /**
     * Constant stolen from Math.DEGREES_TO_RADIANS
     */
    public static final float DEG_TO_RAD = 0.017453292519943295f;
}
