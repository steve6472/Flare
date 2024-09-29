package steve6472.test;

import org.lwjgl.glfw.GLFW;
import steve6472.core.setting.IntSetting;
import steve6472.core.setting.SettingRegister;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public class TestSettings extends SettingRegister
{
    static { REGISTRY = TestRegistries.SETTING; }

    public static final IntSetting CLOSE = registerInt("close", GLFW.GLFW_KEY_ESCAPE);

    public static IntSetting KEY_MOVE_LEFT = registerInt("key_move_left", GLFW_KEY_A);
    public static IntSetting KEY_MOVE_RIGHT = registerInt("key_move_right", GLFW_KEY_D);
    public static IntSetting KEY_MOVE_FORWARD = registerInt("key_move_forward", GLFW_KEY_W);
    public static IntSetting KEY_MOVE_BACKWARD = registerInt("key_move_backward", GLFW_KEY_S);
}
