package steve6472.test;

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

    public static final IntSetting CUBE_AMOUNT = registerInt("cube_amount", 8);
    public static final IntSetting SPHERE_AMOUNT = registerInt("sphere_amount", 8);
}
