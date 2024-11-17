package steve6472.flare.input;

import steve6472.core.registry.ObjectRegistry;
import steve6472.core.registry.Registry;

import java.util.Collection;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
public class KeybindUpdater
{
    public static void updateKeybinds(Collection<Keybind> keybinds, UserInput input)
    {
        keybinds.forEach(keybind -> keybind.input = input);
    }

    public static void updateKeybinds(ObjectRegistry<Keybind> keybinds, UserInput input)
    {
        keybinds.keys().forEach(key -> keybinds.get(key).input = input);
    }

    public static void updateKeybinds(Registry<Keybind> keybinds, UserInput input)
    {
        keybinds.getMap().forEach((_, keybind) -> keybind.input = input);
    }
}
