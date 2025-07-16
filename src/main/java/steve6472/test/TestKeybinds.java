package steve6472.test;

import org.lwjgl.glfw.GLFW;
import steve6472.core.registry.Key;
import steve6472.flare.FlareConstants;
import steve6472.flare.input.Keybind;
import steve6472.flare.input.KeybindType;

/**
 * Created by steve6472
 * Date: 9/30/2024
 * Project: Flare <br>
 */
public class TestKeybinds
{
    public static final Keybind FORWARD = register(new Keybind(FlareConstants.key("forward"), KeybindType.REPEAT, GLFW.GLFW_KEY_W));
    public static final Keybind LEFT = register(new Keybind(FlareConstants.key("left"), KeybindType.REPEAT, GLFW.GLFW_KEY_A));
    public static final Keybind RIGHT = register(new Keybind(FlareConstants.key("right"), KeybindType.REPEAT, GLFW.GLFW_KEY_D));
    public static final Keybind BACK = register(new Keybind(FlareConstants.key("back"), KeybindType.REPEAT, GLFW.GLFW_KEY_S));

    public static final Keybind TO_UP = register(new Keybind(FlareConstants.key("to_up"), KeybindType.ONCE, GLFW.GLFW_KEY_UP));
    public static final Keybind TO_LEFT = register(new Keybind(FlareConstants.key("to_left"), KeybindType.ONCE, GLFW.GLFW_KEY_LEFT));
    public static final Keybind TO_RIGHT = register(new Keybind(FlareConstants.key("to_right"), KeybindType.ONCE, GLFW.GLFW_KEY_RIGHT));
    public static final Keybind TO_DOWN = register(new Keybind(FlareConstants.key("to_down"), KeybindType.ONCE, GLFW.GLFW_KEY_DOWN));

    private static Keybind register(Keybind keybind)
    {
        TestRegistries.KEYBIND.register(keybind);
        return keybind;
    }
}
