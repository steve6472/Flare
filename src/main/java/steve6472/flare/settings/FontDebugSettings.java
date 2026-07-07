package steve6472.flare.settings;

import steve6472.core.registry.Registry;
import steve6472.core.setting.BoolSetting;
import steve6472.core.setting.Setting;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
public class FontDebugSettings
{
    public static BoolSetting BASELINE;

    public static BoolSetting CHARACTER;
    public static BoolSetting CHARACTER_ASCENT;
    public static BoolSetting CHARACTER_DESCENT;
    public static BoolSetting CHARACTER_ADVANCE;
    public static BoolSetting CHARACTER_UNDERLINE;
    public static BoolSetting CHARACTER_KERN;

    public static BoolSetting SEGMENT;
    public static BoolSetting SEGMENT_MIN_DESCENT;
    public static BoolSetting SEGMENT_MAX_DESCENT;

    public static BoolSetting MESSAGE_ORIGIN;
    public static BoolSetting MESSAGE_MAX_WIDTH;
    public static BoolSetting MESSAGE_MAX_HEIGHT;
    public static BoolSetting MESSAGE_ANCHORS;

    public static void bootstrap(Registry<Setting<?, ?>> registry)
    {
        BASELINE = Setting.registerBool(registry, "baseline", false);

        CHARACTER = Setting.registerBool(registry, "character", false);
        CHARACTER_ASCENT = Setting.registerBool(registry, "character_ascent", false);
        CHARACTER_DESCENT = Setting.registerBool(registry, "character_descent", false);
        CHARACTER_ADVANCE = Setting.registerBool(registry, "character_advance", false);
        CHARACTER_UNDERLINE = Setting.registerBool(registry, "character_underline", false);
        CHARACTER_KERN = Setting.registerBool(registry, "character_kern", false);

        SEGMENT = Setting.registerBool(registry, "segment", false);
        SEGMENT_MIN_DESCENT = Setting.registerBool(registry, "segment_min_descent", false);
        SEGMENT_MAX_DESCENT = Setting.registerBool(registry, "segment_max_descent", false);

        MESSAGE_ORIGIN = Setting.registerBool(registry, "message_origin", false);
        MESSAGE_MAX_WIDTH = Setting.registerBool(registry, "message_max_width", false);
        MESSAGE_MAX_HEIGHT = Setting.registerBool(registry, "message_max_height", false);
        MESSAGE_ANCHORS = Setting.registerBool(registry, "message_anchors", false);
    }
}
