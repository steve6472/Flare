package steve6472.test;

import org.lwjgl.glfw.GLFW;
import steve6472.core.setting.IntSetting;
import steve6472.core.setting.SettingRegister;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public class TestSettings extends SettingRegister
{
    static { REGISTRY = TestRegistries.SETTING; }

    public static final IntSetting CLOSE = registerInt("close", GLFW.GLFW_KEY_ESCAPE);
}
