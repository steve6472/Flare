package steve6472.flare.settings;

import steve6472.core.setting.BoolSetting;
import steve6472.core.setting.SettingRegister;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
public class FontDebugSettings extends SettingRegister
{
    static {
        REGISTRY = FlareRegistries.FONT_DEBUG_SETTINGS;
        NAMESPACE = FlareConstants.NAMESPACE;
    }

    public static BoolSetting BASELINE = registerBool("baseline", false);

    public static BoolSetting CHARACTER = registerBool("character", false);
    public static BoolSetting CHARACTER_ASCENT = registerBool("character_ascent", false);
    public static BoolSetting CHARACTER_DESCENT = registerBool("character_descent", false);
    public static BoolSetting CHARACTER_ADVANCE = registerBool("character_advance", false);
    public static BoolSetting CHARACTER_UNDERLINE = registerBool("character_underline", false);
    public static BoolSetting CHARACTER_KERN = registerBool("character_kern", false);

    public static BoolSetting SEGMENT = registerBool("segment", false);
    public static BoolSetting SEGMENT_MIN_DESCENT = registerBool("segment_min_descent", false);
    public static BoolSetting SEGMENT_MAX_DESCENT = registerBool("segment_max_descent", false);

    public static BoolSetting MESSAGE_ORIGIN = registerBool("message_origin", false);
    public static BoolSetting MESSAGE_MAX_WIDTH = registerBool("message_max_width", false);
    public static BoolSetting MESSAGE_ANCHORS = registerBool("message_anchors", false);
}
