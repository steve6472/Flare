package steve6472.test;

import org.lwjgl.glfw.GLFW;
import steve6472.core.registry.Key;
import steve6472.volkaniums.input.Keybind;
import steve6472.volkaniums.input.KeybindType;

/**
 * Created by steve6472
 * Date: 9/30/2024
 * Project: Volkaniums <br>
 */
public class TestKeybinds
{
    public static final Keybind FORWARD = register(new Keybind(Key.defaultNamespace("forward"), KeybindType.REPEAT, GLFW.GLFW_KEY_W));
    public static final Keybind LEFT = register(new Keybind(Key.defaultNamespace("left"), KeybindType.REPEAT, GLFW.GLFW_KEY_A));
    public static final Keybind RIGHT = register(new Keybind(Key.defaultNamespace("right"), KeybindType.REPEAT, GLFW.GLFW_KEY_D));
    public static final Keybind BACK = register(new Keybind(Key.defaultNamespace("back"), KeybindType.REPEAT, GLFW.GLFW_KEY_S));

    private static Keybind register(Keybind keybind)
    {
        TestRegistries.KEYBIND.register(keybind);
        return keybind;
    }
}
