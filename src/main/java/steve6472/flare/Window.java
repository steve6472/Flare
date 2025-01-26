package steve6472.flare;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryUtil;
import steve6472.core.log.Log;

import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Flare <br>
 */
public class Window
{
    private static final Logger LOGGER = Log.getLogger(Window.class);

    public static final int WIDTH = 16 * 70;
    public static final int HEIGHT = 9 * 70;

    private long window;
    boolean framebufferResize;
    private int currentWidth = WIDTH;
    private int currentHeight = HEIGHT;
    private boolean closeWindow;
    private WindowCallbacks callbacks;

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

        createCallbacks();
    }

    private void createCallbacks()
    {
        callbacks = new WindowCallbacks();

        callbacks.addFramebufferSizeCallback(FlareConstants.flareKey("main"), this::framebufferResizeCallback);
        callbacks.addErrorCallback(FlareConstants.flareKey("main"), (error, description) -> LOGGER.severe("GLFW Error: " + error + " -> " + description));

        callbacks.addDropCallback(FlareConstants.flareKey("debug"), (_, count, names) -> {
            PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
            LOGGER.finest("Drop " + count + " items:");
            for (int i = 0; i < count; i++)
            {
                String name = MemoryUtil.memUTF8(charPointers.get(i));
                LOGGER.finest("\tPath: " + name);
            }
        });

        glfwSetFramebufferSizeCallback(window, (handle, width, height) -> callbacks.runCallbacks(GLFWFramebufferSizeCallbackI.class, callback -> callback.invoke(handle, width, height)));
        glfwSetScrollCallback(window, (handle, x, y) -> callbacks.runCallbacks(GLFWScrollCallbackI.class, callback -> callback.invoke(handle, x, y)));
        glfwSetKeyCallback(window, (handle, key, scancode, action, mods) -> callbacks.runCallbacks(GLFWKeyCallbackI.class, callback -> callback.invoke(handle, key, scancode, action, mods)));
        glfwSetCharCallback(window, (handle, codepoint) -> callbacks.runCallbacks(GLFWCharCallbackI.class, callback -> callback.invoke(handle, codepoint)));
        glfwSetCursorEnterCallback(window, (handle, entered) -> callbacks.runCallbacks(GLFWCursorEnterCallbackI.class, callback -> callback.invoke(handle, entered)));
        glfwSetCursorPosCallback(window, (handle, xpos, ypos) -> callbacks.runCallbacks(GLFWCursorPosCallbackI.class, callback -> callback.invoke(handle, xpos, ypos)));
        glfwSetJoystickCallback((jid, event) -> callbacks.runCallbacks(GLFWJoystickCallbackI.class, callback -> callback.invoke(jid, event)));
        glfwSetMouseButtonCallback(window, (handle, button, action, mods) -> callbacks.runCallbacks(GLFWMouseButtonCallbackI.class, callback -> callback.invoke(handle, button, action, mods)));
        glfwSetMonitorCallback((monitor, event) -> callbacks.runCallbacks(GLFWMonitorCallbackI.class, callback -> callback.invoke(monitor, event)));
        glfwSetErrorCallback((error, description) -> callbacks.runCallbacks(GLFWErrorCallbackI.class, callback -> callback.invoke(error, description)));
        glfwSetWindowIconifyCallback(window, (handle, iconified) -> callbacks.runCallbacks(GLFWWindowIconifyCallbackI.class, callback -> callback.invoke(handle, iconified)));
        glfwSetWindowFocusCallback(window, (handle, focused) -> callbacks.runCallbacks(GLFWWindowFocusCallbackI.class, callback -> callback.invoke(handle, focused)));
        glfwSetWindowRefreshCallback(window, (handle) -> callbacks.runCallbacks(GLFWWindowRefreshCallbackI.class, callback -> callback.invoke(handle)));
        glfwSetWindowMaximizeCallback(window, (handle, maximized) -> callbacks.runCallbacks(GLFWWindowMaximizeCallbackI.class, callback -> callback.invoke(handle, maximized)));
        glfwSetDropCallback(window, (handle, count, names) -> callbacks.runCallbacks(GLFWDropCallbackI.class, callback -> callback.invoke(handle, count, names)));
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

    public WindowCallbacks callbacks()
    {
        return callbacks;
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
        return glfwWindowShouldClose(window) || closeWindow;
    }

    public void closeWindow()
    {
        closeWindow = true;
    }

    public boolean isFocused()
    {
        return glfwGetWindowAttrib(window, GLFW_FOCUSED) == GLFW_TRUE;
    }

    public void cleanup()
    {
        callbacks.cleanup(window);
    }
}
