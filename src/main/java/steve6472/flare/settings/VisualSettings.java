package steve6472.flare.settings;

import steve6472.core.setting.*;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class VisualSettings extends SettingRegister
{
    static {
        REGISTRY = FlareRegistries.VISUAL_SETTINGS;
        NAMESPACE = FlareConstants.NAMESPACE;
    }

    /// Dummy value mainly for bootstrap
    public static StringSetting USERNAME = registerString("username", "Steve");

    /*
     * Internal
     */

    public static EnumSetting<PresentMode> PRESENT_MODE = registerEnum("present_mode", PresentMode.FIFO);
    public static IntSetting GLOBAL_CAMERA_COUNT = registerInt("global_camera_count", 3);

    /// Max timeout for font generating in seconds
    public static IntSetting FONT_GENERATE_TIMEOUT = registerInt("font_generate_timeout", 15);

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
    public static BoolSetting RENDER_CENTER_POINT = registerBool("render_center_point", false);

    /// Updates the window title with avarage FPS over last second and last frame MS
    public static BoolSetting TITLE_FPS = registerBool("title_fps", false);

    /// Creates a json of name - coordinate pairs for each atlas at startup
    public static BoolSetting GENERATE_STARTUP_ATLAS_DATA = registerBool("generate_startup_atlas_data", false);

    /// Validation level
    /// A warning is thrown if validation level is set to anything other than NONE and no Vulkan SDK is found
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL = registerEnum("validation_level", ValidationLevel.NONE);

    /// If true, logs everything from the font generator process
    /// (default false as it is mostly missing glyphs)
    /// Does not exactly work 'cause the fonts are generated before the settings are read....
    public static BoolSetting FONT_GEN_LOGS = registerBool("font_gen_logs", false);
}
