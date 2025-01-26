package steve6472.flare;

import org.lwjgl.glfw.*;
import org.lwjgl.system.CallbackI;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 1/26/2025
 * Project: Flare <br>
 */
public class WindowCallbacks
{
    private static final Logger LOGGER = Log.getLogger(WindowCallbacks.class);
    private final Map<Class<?>, Map<Key, CallbackI>> callbacks = new HashMap<>();

    public static boolean LOG_CALLBACKS = false;

    /**
     * Adds a framebuffer size callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetFramebufferSizeCallback
     */
    public void addFramebufferSizeCallback(Key key, GLFWFramebufferSizeCallbackI callback)
    {
        registerCallback(GLFWFramebufferSizeCallbackI.class, transformKey(key, "framebuffer_size"), callback);
    }

    /**
     * Adds a scroll callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetScrollCallback
     */
    public void addScrollCallback(Key key, GLFWScrollCallbackI callback)
    {
        registerCallback(GLFWScrollCallbackI.class, transformKey(key, "scroll"), callback);
    }

    /**
     * Adds a key callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetKeyCallback
     */
    public void addKeyCallback(Key key, GLFWKeyCallbackI callback)
    {
        registerCallback(GLFWKeyCallbackI.class, transformKey(key, "key"), callback);
    }

    /**
     * Adds a character callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetCharCallback
     */
    public void addCharCallback(Key key, GLFWCharCallbackI callback)
    {
        registerCallback(GLFWCharCallbackI.class, transformKey(key, "char"), callback);
    }

    /**
     * Adds a drop callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetDropCallback
     */
    public void addDropCallback(Key key, GLFWDropCallbackI callback)
    {
        registerCallback(GLFWDropCallbackI.class, transformKey(key, "drop"), callback);
    }

    /**
     * Adds a cursor enter callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetCursorEnterCallback
     */
    public void addCursorEnterCallback(Key key, GLFWCursorEnterCallbackI callback)
    {
        registerCallback(GLFWCursorEnterCallbackI.class, transformKey(key, "cursor_enter"), callback);
    }

    /**
     * Adds a cursor position callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetCursorPosCallback
     */
    public void addCursorPosCallback(Key key, GLFWCursorPosCallbackI callback)
    {
        registerCallback(GLFWCursorPosCallbackI.class, transformKey(key, "cursor_pos"), callback);
    }

    /**
     * Adds a joystick callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetJoystickCallback
     */
    public void addJoystickCallback(Key key, GLFWJoystickCallbackI callback)
    {
        registerCallback(GLFWJoystickCallbackI.class, transformKey(key, "joystick"), callback);
    }

    /**
     * Adds a mouse button callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetMouseButtonCallback
     */
    public void addMouseButtonCallback(Key key, GLFWMouseButtonCallbackI callback)
    {
        registerCallback(GLFWMouseButtonCallbackI.class, transformKey(key, "mouse_button"), callback);
    }

    /**
     * Adds a monitor callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetMonitorCallback
     */
    public void addMonitorCallback(Key key, GLFWMonitorCallbackI callback)
    {
        registerCallback(GLFWMonitorCallbackI.class, transformKey(key, "monitor"), callback);
    }

    /**
     * Adds an error callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetErrorCallback
     */
    public void addErrorCallback(Key key, GLFWErrorCallbackI callback)
    {
        registerCallback(GLFWErrorCallbackI.class, transformKey(key, "error"), callback);
    }

    /**
     * Adds a window iconify callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetWindowIconifyCallback
     */
    public void addWindowIconifyCallback(Key key, GLFWWindowIconifyCallbackI callback)
    {
        registerCallback(GLFWWindowIconifyCallbackI.class, transformKey(key, "window_iconify"), callback);
    }

    /**
     * Adds a window focus callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetWindowFocusCallback
     */
    public void addWindowFocusCallback(Key key, GLFWWindowFocusCallbackI callback)
    {
        registerCallback(GLFWWindowFocusCallbackI.class, transformKey(key, "window_focus"), callback);
    }

    /**
     * Adds a window refresh callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetWindowRefreshCallback
     */
    public void addWindowRefreshCallback(Key key, GLFWWindowRefreshCallbackI callback)
    {
        registerCallback(GLFWWindowRefreshCallbackI.class, transformKey(key, "window_refresh"), callback);
    }

    /**
     * Adds a window maximize callback.
     *
     * @see org.lwjgl.glfw.GLFW#glfwSetWindowMaximizeCallback
     */
    public void addWindowMaximizeCallback(Key key, GLFWWindowMaximizeCallbackI callback)
    {
        registerCallback(GLFWWindowMaximizeCallbackI.class, transformKey(key, "window_maximize"), callback);
    }

    /*
     *
     */

    private <T extends CallbackI> void registerCallback(Class<T> clazz, Key key, T callback)
    {
        callbacks.computeIfAbsent(clazz, _ -> new HashMap<>()).put(key, callback);
    }

    private Key transformKey(Key key, String prefix)
    {
        return Key.withNamespace(key.namespace(), prefix + "/" + key.id());
    }

    <T extends CallbackI> void runCallbacks(Class<T> type, Consumer<T> callback)
    {
        Map<Key, CallbackI> keyCallbackMap = callbacks.get(type);
        if (keyCallbackMap == null)
            return;

        if (LOG_CALLBACKS)
            LOGGER.finest("Callback %s:".formatted(type.getSimpleName()));

        for (Key key : keyCallbackMap.keySet())
        {
            CallbackI value = keyCallbackMap.get(key);
            try
            {
                if (LOG_CALLBACKS)
                    LOGGER.finest("\t%s".formatted(key));
                callback.accept(type.cast(value));
            } catch (Exception exception)
            {
                LOGGER.severe("Callback %s threw an exception".formatted(key));
                exception.printStackTrace();
            }
        }
    }

    void cleanup(long window)
    {
        Callbacks.glfwFreeCallbacks(window);
        callbacks.clear();
    }
}
