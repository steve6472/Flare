package steve6472.flare.tracy;

/**
 * Created by steve6472
 * Date: 11/5/2025
 * Project: Orbiter <br>
 */
public interface Profiler
{
    /*
        Tracy native stuff:
        public static native void startupProfiler();
        public static native void shutdownProfiler();
        public static native boolean isConnected();
        public static native void markFrame();
        public static native long allocSourceLocation(int line, String source, String function, String name, int colour);
        public static native ZoneContext zoneBegin(long sourceLocation, int active);
        public static native void zoneEnd(ZoneContext zoneContext);
     */

    void start();
    void end();

    void push(String name, int color);
    void pop();

    void push(String name);
    void popPush(String name);
    void popPush(String name, int color);
}
