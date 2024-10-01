package steve6472.volkaniums.settings;

import steve6472.core.setting.*;
import steve6472.volkaniums.registry.VolkaniumsRegistries;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class VisualSettings extends SettingRegister
{
    static { REGISTRY = VolkaniumsRegistries.VISUAL_SETTINGS; }

    /// Dummy value mainly for bootstrap
    public static StringSetting USERNAME = registerString("username", "Steve");
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL = registerEnum("validation_level", ValidationLevel.WARNING);

    /*
     * Generic
     */

    public static IntSetting FOV = registerInt("fov", 90);

    /*
     * Internal
     */

    public static EnumSetting<PresentMode> PRESENT_MODE = registerEnum("present_mode", PresentMode.MAILBOX);
    public static IntSetting GLOBAL_CAMERA_COUNT = registerInt("global_camera_count", 3);

    /*
     * Vr
     */

    /// Master toggle for... VR!?
    public static BoolSetting VR = registerBool("vr", false);

    /// Enable this so debug lines can render in VR
    /// Is slower and limited to only 262144 verticies (might add setting ?)
    public static BoolSetting DEBUG_LINE_SINGLE_BUFFER = registerBool("debug_line_single_buffer", false);

    /*
     * Debug
     */

    /// Can be disabled if Graphics Card does not support wide lines
    /// _(disable implementation not fully implemented)_
    public static BoolSetting ENABLE_WIDE_LINES = registerBool("enable_wide_lines", true);

    /// Only applied if [#ENABLE_WIDE_LINES] is `true`
    public static FloatSetting LINE_WIDTH = registerFloat("line_width", 4.0f);

    /// Toggles a gray cross at 0, 0, 0
    public static BoolSetting RENDER_CENTER_POINT = registerBool("render_center_point", true);

    /// Updates the window title with avarage FPS over last second and last frame MS
    public static BoolSetting TITLE_FPS = registerBool("title_fps", true);
}
