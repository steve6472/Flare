package steve6472.test;

import steve6472.core.registry.ObjectRegistry;
import steve6472.core.registry.Registry;
import steve6472.core.setting.Setting;
import steve6472.flare.input.Keybind;
import steve6472.flare.registry.RegistryCreators;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestRegistries extends RegistryCreators
{
    static {
        NAMESPACE = TestApp.instance.defaultNamespace();
    }

    public static final Registry<Rarity> RARITY = createRegistry("rarity", () -> Rarities.COMMON);
    public static final ObjectRegistry<Setting<?, ?>> SETTING = createObjectRegistry("setting", () -> TestSettings.CUBE_AMOUNT);
    public static final Registry<Keybind> KEYBIND = createRegistry("keybind", () -> TestKeybinds.FORWARD);
}
