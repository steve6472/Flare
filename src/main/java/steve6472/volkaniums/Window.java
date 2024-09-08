package steve6472.volkaniums;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public class Window
{
    private static final String WINDOW_TITLE = "Volkaniums";

    public static final int WIDTH = 16 * 70;
    public static final int HEIGHT = 9 * 70;

    private long window;
    boolean framebufferResize;

    public Window()
    {
        initWindow();
    }

    private void initWindow()
    {
        if (!glfwInit())
        {
            throw new RuntimeException(ErrorCode.GLFW_INIT.format());
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(WIDTH, HEIGHT, WINDOW_TITLE, NULL, NULL);

        if (window == NULL)
        {
            throw new RuntimeException(ErrorCode.WINDOW_CREATION.format());
        }

        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);
    }

    private void framebufferResizeCallback(long window, int width, int height)
    {
        framebufferResize = true;
    }

    public long window()
    {
        return window;
    }

    public boolean isFramebufferResize()
    {
        return framebufferResize;
    }

    public void resetFramebufferResizeFlag()
    {
        framebufferResize = false;
    }

    public void destroyWindow()
    {
        glfwDestroyWindow(window);
    }

    public boolean shouldWindowClose()
    {
        return glfwWindowShouldClose(window);
    }

    public boolean isFocused()
    {
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE;
    }
}
