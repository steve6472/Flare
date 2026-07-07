package steve6472.test;

import steve6472.core.registry.Registry;
import steve6472.core.setting.IntSetting;
import steve6472.core.setting.Setting;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestSettings
{
    public static IntSetting CUBE_AMOUNT;
    public static IntSetting SPHERE_AMOUNT;

    public static IntSetting FOV;

    public static void bootstrap(Registry<Setting<?, ?>> registry)
    {
        CUBE_AMOUNT = Setting.registerInt(registry, "cube_amount", 8);
        SPHERE_AMOUNT = Setting.registerInt(registry, "sphere_amount", 8);

        FOV = Setting.registerInt(registry, "fov", 90);
    }
}
