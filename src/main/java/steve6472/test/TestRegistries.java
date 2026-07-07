package steve6472.test;

import steve6472.core.registry.Registry;
import steve6472.core.registry.ResourceKey;
import steve6472.core.setting.Setting;
import steve6472.flare.input.Keybind;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestRegistries
{
    static final ResourceKey<Registry<Setting<?, ?>>> SETTING = ResourceKey.createRegistryKey(TestApp.key("setting"));

    static final ResourceKey<Registry<Keybind>> KEYBIND = ResourceKey.createRegistryKey(TestApp.key("keybind"));
}
