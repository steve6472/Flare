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

    /*
     * Internal
     */

    public static EnumSetting<PresentMode> PRESENT_MODE = registerEnum("present_mode", PresentMode.MAILBOX);
    public static IntSetting GLOBAL_CAMERA_COUNT = registerInt("global_camera_count", 3);

    /// Path to the font file (only one font can be loaded in the whole app for now)
    public static StringSetting FONT_PATH = registerString("font_path", "resources/font/cmunrm.ttf");

    /// Max timeout for font generating
    public static IntSetting FONT_GENERATE_TIMEOUT = registerInt("font_generate_timeout", 15);

    /// Generate font on startup
    /// Automatically set to false after font is generated
    public static BoolSetting GENERATE_FONT = registerBool("generate_font", true);

    /*
     * Vr
     */

    /// Master toggle for... VR!?
    public static BoolSetting VR = registerBool("vr", false);

    /// Enable this so debug lines can render in VR
    /// Is slower and limited to only 262144 verticies (might add setting ?)
    public static BoolSetting DEBUG_LINE_SINGLE_BUFFER = registerBool("debug_line_single_buffer", false);

    /// Path to the Steam VR Action Manifest
    public static StringSetting ACTION_MANIFEST_PATH = registerString("vr_action_manifest_path", "settings/vr_actions.json");

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

    /// Validation level
    /// A warning is thrown if validation level is set to anything other than NONE and no Vulkan SDK is found
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL = registerEnum("validation_level", ValidationLevel.WARNING);

    /// If true, logs everything from the font generator process
    /// (default false as it is mostly missing glyphs)
    public static BoolSetting FONT_GEN_LOGS = registerBool("font_gen_logs", false);
}
