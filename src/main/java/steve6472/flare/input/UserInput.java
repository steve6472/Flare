package steve6472.flare.input;

import org.joml.Vector2i;
import steve6472.core.setting.IntSetting;
import steve6472.flare.Window;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Created by steve6472
 * Date: 8/18/2024
 * Project: Flare <br>
 */
public class UserInput
{
    private final Window window;

    public UserInput(Window window)
    {
        this.window = window;
    }

    public boolean isKeyPressed(IntSetting keybind)
    {
        return glfwGetKey(window.window(), keybind.get()) == GLFW_PRESS;
    }

    public boolean isKeyPressed(int key)
    {
        return glfwGetKey(window.window(), key) == GLFW_PRESS;
    }

    public boolean isMouseButtonPressed(int button)
    {
        return glfwGetMouseButton(window.window(), button) == GLFW_PRESS;
    }

    public Vector2i getMousePositionRelativeToTopLeftOfTheWindow()
    {
        double[] x = new double[1];
        double[] y = new double[1];
        glfwGetCursorPos(window.window(), x, y);
        return new Vector2i((int) x[0], (int) y[0]);
    }
}
