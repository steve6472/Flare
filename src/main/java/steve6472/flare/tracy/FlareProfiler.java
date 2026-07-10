package steve6472.flare.tracy;

import io.github.benjaminamos.tracy.Tracy;
import steve6472.flare.FlareConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 11/5/2025
 * Project: Orbiter <br>
 */
public class FlareProfiler
{
    public static final boolean ENABLE_TRACY = FlareConstants.SystemProperties.booleanProperty(FlareConstants.SystemProperties.ENABLE_TRACY);

    private final static Map<String, Profiler> PROFILERS = new HashMap<>();

    public static Profiler get()
    {
        return get("main", TracyColors.RANDOM);
    }

    public static Profiler frame()
    {
        return get("Frame", TracyColors.FRAME);
    }

    public static Profiler startup()
    {
        return get("Flare Startup", TracyColors.STARTUP);
    }

    public static Profiler cleanup()
    {
        return get("Flare Clenaup", TracyColors.CLEANUP);
    }

    public static Profiler world()
    {
        return get("World", TracyColors.RANDOM);
    }

    public static Profiler network()
    {
        return get("Network", TracyColors.NETWORK);
    }

    public static Profiler get(String name, int color)
    {
        return PROFILERS.computeIfAbsent(name, n -> {
            if (ENABLE_TRACY)
                return new TracyProfiler(n, color);
            else
                return new EmptyProfiler();
        });
    }

    public static void endFrame()
    {
        if (ENABLE_TRACY)
            Tracy.markFrame();
    }

    public static void shutdown()
    {
        if (ENABLE_TRACY)
            Tracy.shutdownProfiler();
    }

    public static void message(String message)
    {
        if (ENABLE_TRACY)
            Tracy.message(message);
    }

    public static void plot(String name, float value)
    {
        if (ENABLE_TRACY)
            Tracy.plotFloat(name, value);
    }

    public static void plot(String name, double value)
    {
        if (ENABLE_TRACY)
            Tracy.plot(name, value);
    }
}