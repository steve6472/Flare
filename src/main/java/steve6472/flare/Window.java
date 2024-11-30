package steve6472.flare;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Flare <br>
 */
public class Window
{
    public static final int WIDTH = 16 * 70;
    public static final int HEIGHT = 9 * 70;

    private long window;
    boolean framebufferResize;
    private int currentWidth = WIDTH;
    private int currentHeight = HEIGHT;

    public Window(String title)
    {
        initWindow(title);
    }

    private void initWindow(String title)
    {
        if (!glfwInit())
        {
            throw new RuntimeException(ErrorCode.GLFW_INIT.format());
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(WIDTH, HEIGHT, title, NULL, NULL);

        if (window == NULL)
        {
            throw new RuntimeException(ErrorCode.WINDOW_CREATION.format());
        }

        glfwSetFramebufferSizeCallback(window, this::framebufferResizeCallback);
    }

    public void setWindowTitle(String title)
    {
        glfwSetWindowTitle(window, title);
    }

    private void framebufferResizeCallback(long window, int width, int height)
    {
        framebufferResize = true;
        currentWidth = width;
        currentHeight = height;
    }

    public int getWidth()
    {
        return currentWidth;
    }

    public int getHeight()
    {
        return currentHeight;
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
