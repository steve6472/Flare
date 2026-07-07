package steve6472.flare.settings;

import steve6472.core.registry.Registry;
import steve6472.core.setting.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class VisualSettings
{
    /// Dummy value mainly for bootstrap
    public static StringSetting USERNAME;

    /*
     * Internal
     */

    public static EnumSetting<PresentMode> PRESENT_MODE;
    public static IntSetting GLOBAL_CAMERA_COUNT;

    /// Max timeout for font generating in seconds
    public static IntSetting FONT_GENERATE_TIMEOUT;

    /*
     * Vr
     */

    /// Master toggle for... VR!?
    @Deprecated
    public static BoolSetting VR;

    /// Enable this so debug lines can render in VR
    /// Is slower and limited to only 262144 verticies (might add setting ?)
    public static BoolSetting DEBUG_LINE_SINGLE_BUFFER;

    /// Path to the Steam VR Action Manifest
    public static StringSetting ACTION_MANIFEST_PATH;

    /*
     * Debug
     */

    /// Can be disabled if Graphics Card does not support wide lines
    /// _(disable implementation not fully implemented)_
    public static BoolSetting ENABLE_WIDE_LINES;

    /// Only applied if [#ENABLE_WIDE_LINES] is `true`
    public static FloatSetting LINE_WIDTH;

    /// Toggles a gray cross at 0, 0, 0
    public static BoolSetting RENDER_CENTER_POINT ;

    /// Updates the window title with avarage FPS over last second and last frame MS
    public static BoolSetting TITLE_FPS;

    /// Creates a json of name - coordinate pairs for each atlas at startup
    public static BoolSetting GENERATE_STARTUP_ATLAS_DATA;

    /// Validation level
    /// A warning is thrown if validation level is set to anything other than NONE and no Vulkan SDK is found
    public static EnumSetting<ValidationLevel> VALIDATION_LEVEL;

    /// If true, logs everything from the font generator process
    /// (default false as it is mostly missing glyphs)
    /// Does not exactly work 'cause the fonts are generated before the settings are read....
    public static BoolSetting FONT_GEN_LOGS;

    public static void bootstrap(Registry<Setting<?, ?>> registry)
    {
        USERNAME = Setting.registerString(registry, "username", "Steve");
        PRESENT_MODE = Setting.registerEnum(registry, "present_mode", PresentMode.FIFO);
        GLOBAL_CAMERA_COUNT = Setting.registerInt(registry, "global_camera_count", 3);
        FONT_GENERATE_TIMEOUT = Setting.registerInt(registry, "font_generate_timeout", 15);
        VR = Setting.registerBool(registry, "vr", false);
        DEBUG_LINE_SINGLE_BUFFER = Setting.registerBool(registry, "debug_line_single_buffer", false);
        ACTION_MANIFEST_PATH = Setting.registerString(registry, "vr_action_manifest_path", "settings/vr_actions.json");
        ENABLE_WIDE_LINES = Setting.registerBool(registry, "enable_wide_lines", true);
        LINE_WIDTH = Setting.registerFloat(registry, "line_width", 4.0f);
        RENDER_CENTER_POINT = Setting.registerBool(registry, "render_center_point", false);
        TITLE_FPS = Setting.registerBool(registry, "title_fps", false);
        GENERATE_STARTUP_ATLAS_DATA = Setting.registerBool(registry, "generate_startup_atlas_data", false);
        VALIDATION_LEVEL = Setting.registerEnum(registry, "validation_level", ValidationLevel.NONE);
        FONT_GEN_LOGS = Setting.registerBool(registry, "font_gen_logs", false);
    }
}
