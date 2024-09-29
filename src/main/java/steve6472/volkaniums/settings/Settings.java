package steve6472.volkaniums.settings;

import steve6472.core.setting.*;
import steve6472.volkaniums.Registries;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class Settings extends SettingRegister
{
    static { REGISTRY = Registries.SETTINGS; }

    /// Dummy value mainly for bootstrap
    public static StringSetting USERNAME = registerString("username", "Steve");
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL = registerEnum("validation_level", ValidationLevel.INFO);

    /*
     * Graphics
     */
    public static EnumSetting<PresentMode> PRESENT_MODE = registerEnum("present_mode", PresentMode.MAILBOX);
    public static IntSetting FOV = registerInt("fov", 90);
    /// Can be disabled if Graphics Card does not support wide lines
    /// _(disable implementation not fully implemented)_
    public static BoolSetting ENABLE_WIDE_LINES = registerBool("enable_wide_lines", true);
    /// Only applied if [#ENABLE_WIDE_LINES] is `true`
    public static FloatSetting LINE_WIDTH = registerFloat("line_width", 4.0f);
    /// Master toggle for... VR!?
    public static BoolSetting VR = registerBool("vr", false);
    /// Enable this so debug lines can render in VR
    /// Is slower and limited to only 262144 verticies (might add setting ?)
    public static BoolSetting DEBUG_LINE_SINGLE_BUFFER = registerBool("debug_line_single_buffer", false);

    /*
     * Keyboard
     */

    public static IntSetting KEY_MOVE_LEFT = registerInt("key_move_left", GLFW_KEY_A);
    public static IntSetting KEY_MOVE_RIGHT = registerInt("key_move_right", GLFW_KEY_D);
    public static IntSetting KEY_MOVE_FORWARD = registerInt("key_move_forward", GLFW_KEY_W);
    public static IntSetting KEY_MOVE_BACKWARD = registerInt("key_move_backward", GLFW_KEY_S);
}
