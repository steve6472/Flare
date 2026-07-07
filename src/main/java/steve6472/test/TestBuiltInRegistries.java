package steve6472.test;

import steve6472.core.registry.Registry;
import steve6472.core.registry.RegistryCore;
import steve6472.core.setting.Setting;
import steve6472.flare.input.Keybind;

/**
 * Created by steve6472
 * Date: 7/4/2026
 * Project: Flare <br>
 *
 */
class TestBuiltInRegistries
{
    static final Registry<Setting<?, ?>> SETTING = RegistryCore.createSettingsRegistry(TestRegistries.SETTING, TestSettings::bootstrap);
    static final Registry<Keybind> KEYBIND = RegistryCore.createRegistry(TestRegistries.KEYBIND, TestKeybinds::bootstrap);

    public static void bootstrap() {}
}
