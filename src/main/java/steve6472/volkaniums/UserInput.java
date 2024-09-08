package steve6472.volkaniums;

import org.joml.Vector2i;
import steve6472.volkaniums.settings.Settings;

import static org.lwjgl.glfw.GLFW.*;

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

    public Vector2i getMousePositionRelativeToTopLeftOfTheWindow()
    {
        double[] x = new double[1];
        double[] y = new double[1];
        glfwGetCursorPos(window.window(), x, y);
        return new Vector2i((int) x[0], (int) y[0]);
    }
}
