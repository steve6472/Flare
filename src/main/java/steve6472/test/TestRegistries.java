package steve6472.test;

import steve6472.core.registry.ObjectRegistry;
import steve6472.core.registry.Registry;
import steve6472.core.setting.Setting;
import steve6472.volkaniums.registry.RegistryCreators;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public class TestRegistries extends RegistryCreators
{
    public static final Registry<Rarity> RARITY = createRegistry("rarity", () -> Rarities.COMMON);
    public static final ObjectRegistry<Setting<?, ?>> SETTING = createObjectRegistry("setting", () -> TestSettings.CLOSE);
}
