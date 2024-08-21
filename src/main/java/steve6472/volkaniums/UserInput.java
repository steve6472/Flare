package steve6472.volkaniums;

import steve6472.volkaniums.settings.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Volkaniums <br>
 */
public class UserInput
{
    private final Window window;

    public UserInput(Window window)
    {
        this.window = window;
    }

    public boolean isKeyPressed(Settings.IntSetting keybind)
    {
        return glfwGetKey(window.window(), keybind.get()) == GLFW_PRESS;
    }
}
