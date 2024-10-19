package steve6472.volkaniums.render.debug;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.volkaniums.render.debug.objects.*;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.struct.Struct;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 9/21/2024
 * Project: Volkaniums <br>
 */
@SuppressWarnings("unused")
public class DebugRender
{
    private static DebugRender instance;
    private final List<DebugRenderTime> debugLines;

    private DebugRender()
    {
        debugLines = new ArrayList<>();
    }

    public static DebugRender getInstance()
    {
        if (instance == null)
            instance = new DebugRender();
        return instance;
    }

    /*
     * Main render method
     */

    public List<Struct> createVerticies()
    {
        List<Struct> vertices = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        debugLines.forEach(ren ->
        {
            // TODO: fix scale of 0 not showing anything & fix scaling moving objects towards/from origin
            float t = Math.min(1.0f, (float)(currentTime - ren.startTime()) / (ren.endTime() - ren.startTime()));
            ren.object().addVerticies(vertices, interpolate(ren.transformFrom(), ren.transformTo(), t));
        });
        if (!VisualSettings.DEBUG_LINE_SINGLE_BUFFER.get())
            clearOldVerticies();
        return vertices;
    }

    private static void decompose(Matrix4f mat, Vector3f translation, Quaternionf rotation, Vector3f scale)
    {
        mat.getTranslation(translation);
        mat.getUnnormalizedRotation(rotation);
        mat.getScale(scale);
    }

    private static Matrix4f interpolate(Matrix4f matA, Matrix4f matB, float t)
    {
        Vector3f translationA = new Vector3f(), translationB = new Vector3f();
        Quaternionf rotationA = new Quaternionf(), rotationB = new Quaternionf();
        Vector3f scaleA = new Vector3f(), scaleB = new Vector3f();

        decompose(matA, translationA, rotationA, scaleA);
        decompose(matB, translationB, rotationB, scaleB);

        Vector3f interpolatedTranslation = new Vector3f();
        translationA.lerp(translationB, t, interpolatedTranslation);

        Quaternionf interpolatedRotation = new Quaternionf();
        rotationA.slerp(rotationB, t, interpolatedRotation);

        Vector3f interpolatedScale = new Vector3f();
        scaleA.lerp(scaleB, t, interpolatedScale);

        return new Matrix4f()
            .translate(interpolatedTranslation)
            .rotate(interpolatedRotation)
            .scale(interpolatedScale)
            ;
    }

    public void clearOldVerticies()
    {
        long currentTime = System.currentTimeMillis();
        getInstance().debugLines.removeIf(ren -> ren.endTime() <= currentTime || ren.endTime() == 0);
    }

    /*
     * Adding
     */

    public static void addDebugObjectForFrame(DebugObject debugObject)
    {
        getInstance().debugLines.add(new DebugRenderTime(debugObject, 0, 0, new Matrix4f(), new Matrix4f()));
    }

    public static void addDebugObjectForFrame(DebugObject debugObject, Matrix4f transform)
    {
        getInstance().debugLines.add(new DebugRenderTime(debugObject, 0, 0, transform, transform));
    }

    public static void addDebugObjectForMs(DebugObject debugObject, long ms)
    {
        if (ms <= 0) throw new RuntimeException("time has to be above 0");
        getInstance().debugLines.add(new DebugRenderTime(debugObject, System.currentTimeMillis(), System.currentTimeMillis() + ms, new Matrix4f(), new Matrix4f()));
    }

    public static void addDebugObjectForMs(DebugObject debugObject, long ms, Matrix4f transform)
    {
        if (ms <= 0) throw new RuntimeException("time has to be above 0");
        getInstance().debugLines.add(new DebugRenderTime(debugObject, System.currentTimeMillis(), System.currentTimeMillis() + ms, transform, transform));
    }

    public static void addDebugObjectForMs(DebugObject debugObject, long ms, Matrix4f transformFrom, Matrix4f transformTo)
    {
        if (ms <= 0) throw new RuntimeException("time has to be above 0");
        getInstance().debugLines.add(new DebugRenderTime(debugObject, System.currentTimeMillis(), System.currentTimeMillis() + ms, transformFrom, transformTo));
    }

    public static void addDebugObjectForS(DebugObject debugObject, long s)
    {
        if (s <= 0) throw new RuntimeException("time has to be above 0");
        getInstance().debugLines.add(new DebugRenderTime(debugObject, System.currentTimeMillis(), System.currentTimeMillis() + s * 1000, new Matrix4f(), new Matrix4f()));
    }

    public static void addDebugObjectForS(DebugObject debugObject, long s, Matrix4f transform)
    {
        if (s <= 0) throw new RuntimeException("time has to be above 0");
        getInstance().debugLines.add(new DebugRenderTime(debugObject, System.currentTimeMillis(), System.currentTimeMillis() + s * 1000, transform, transform));
    }

    public static void addDebugObjectForS(DebugObject debugObject, long s, Matrix4f transformFrom, Matrix4f transformTo)
    {
        if (s <= 0) throw new RuntimeException("time has to be above 0");
        getInstance().debugLines.add(new DebugRenderTime(debugObject, System.currentTimeMillis(), System.currentTimeMillis() + s * 1000, transformFrom, transformTo));
    }

    /*
     * Object creation
     */

    public static DebugObject line(Vector3f start, Vector3f end, Vector4f color)
    {
        return new DebugLine(start, end, color);
    }

    public static DebugObject lineCube(Vector3f center, float halfSize, Vector4f color)
    {
        return new DebugCuboid(center, halfSize, color);
    }

    public static DebugObject lineCube(Vector3f center, float halfWidth, float halfHeight, float halfDepth, Vector4f color)
    {
        return new DebugCuboid(center, halfWidth, halfHeight, halfDepth, color);
    }

    public static DebugObject lineCube(Vector3f from, Vector3f to, Vector4f color)
    {
        return new DebugCuboid(from, to, color);
    }

    public static DebugObject cross(Vector3f center, float halfSize, Vector4f color)
    {
        return new DebugCross(center, halfSize, color);
    }

    public static DebugObject lineSphere(float radius, int quality, Vector4f color)
    {
        return new DebugSphere(radius, quality, color);
    }

    public static DebugObject lineCylinder(float height, float radius, int quality, Vector4f color)
    {
        return new DebugCylinder(radius, height, quality, color);
    }

    public static DebugObject lineCapsule(float height, float radius, int quality, Vector4f color)
    {
        return new DebugCapsule(radius, height, quality, color);
    }

    /*
     * Colors
     * Down here 'cause I made too many
     */

    public static final Vector4f RED = new Vector4f(1, 0, 0, 1);
    public static final Vector4f GREEN = new Vector4f(0, 1, 0, 1);
    public static final Vector4f BLUE = new Vector4f(0, 0, 1, 1);
    public static final Vector4f YELLOW = new Vector4f(1, 1, 0, 1);
    public static final Vector4f CYAN = new Vector4f(0, 1, 1, 1);
    public static final Vector4f MAGENTA = new Vector4f(1, 0, 1, 1);
    public static final Vector4f ORANGE = new Vector4f(1, 0.5f, 0, 1);
    public static final Vector4f PURPLE = new Vector4f(0.5f, 0, 0.5f, 1);
    public static final Vector4f PINK = new Vector4f(1, 0.75f, 0.8f, 1);
    public static final Vector4f BROWN = new Vector4f(0.6f, 0.3f, 0, 1);
    public static final Vector4f LIME = new Vector4f(0.75f, 1, 0, 1);
    public static final Vector4f OLIVE = new Vector4f(0.5f, 0.5f, 0, 1);
    public static final Vector4f NAVY = new Vector4f(0, 0, 0.5f, 1);
    public static final Vector4f TEAL = new Vector4f(0, 0.5f, 0.5f, 1);
    public static final Vector4f MAROON = new Vector4f(0.5f, 0, 0, 1);
    public static final Vector4f INDIGO = new Vector4f(0.3f, 0, 0.5f, 1);
    public static final Vector4f GOLD = new Vector4f(1, 0.84f, 0, 1);
    public static final Vector4f SILVER = new Vector4f(0.75f, 0.75f, 0.75f, 1);
    public static final Vector4f GRAY = new Vector4f(0.5f, 0.5f, 0.5f, 1);
    public static final Vector4f LIGHT_GRAY = new Vector4f(0.75f, 0.75f, 0.75f, 1);
    public static final Vector4f DARK_GRAY = new Vector4f(0.25f, 0.25f, 0.25f, 1);
    public static final Vector4f BLACK = new Vector4f(0, 0, 0, 1);
    public static final Vector4f WHITE = new Vector4f(1, 1, 1, 1);
    public static final Vector4f LIGHT_BLUE = new Vector4f(0.68f, 0.85f, 0.9f, 1);
    public static final Vector4f LIGHT_GREEN = new Vector4f(0.56f, 0.93f, 0.56f, 1);
    public static final Vector4f BEIGE = new Vector4f(0.96f, 0.96f, 0.86f, 1);
    public static final Vector4f IVORY = new Vector4f(1, 1, 0.94f, 1);
    public static final Vector4f MINT = new Vector4f(0.6f, 1, 0.6f, 1);
    public static final Vector4f PEACH = new Vector4f(1, 0.85f, 0.73f, 1);
    public static final Vector4f CRIMSON = new Vector4f(0.86f, 0.08f, 0.24f, 1);
    public static final Vector4f AQUA = new Vector4f(0, 1, 1, 1);
    public static final Vector4f CORAL = new Vector4f(1, 0.5f, 0.31f, 1);
    public static final Vector4f SALMON = new Vector4f(0.98f, 0.5f, 0.45f, 1);
    public static final Vector4f CHOCOLATE = new Vector4f(0.82f, 0.41f, 0.12f, 1);
    public static final Vector4f PLUM = new Vector4f(0.87f, 0.63f, 0.87f, 1);
    public static final Vector4f VIOLET = new Vector4f(0.93f, 0.51f, 0.93f, 1);
    public static final Vector4f SKY_BLUE = new Vector4f(0.53f, 0.81f, 0.92f, 1);
    public static final Vector4f KHAKI = new Vector4f(0.94f, 0.9f, 0.55f, 1);
    public static final Vector4f SLATE_GRAY = new Vector4f(0.44f, 0.5f, 0.56f, 1);
    public static final Vector4f DARK_ORANGE = new Vector4f(1, 0.55f, 0, 1);
    public static final Vector4f DARK_RED = new Vector4f(0.55f, 0, 0, 1);
    public static final Vector4f DARK_GREEN = new Vector4f(0, 0.39f, 0, 1);
    public static final Vector4f DARK_BLUE = new Vector4f(0, 0, 0.55f, 1);
    public static final Vector4f DARK_CYAN = new Vector4f(0, 0.55f, 0.55f, 1);
    public static final Vector4f DARK_VIOLET = new Vector4f(0.58f, 0, 0.83f, 1);
    public static final Vector4f DARK_MAGENTA = new Vector4f(0.55f, 0, 0.55f, 1);
    public static final Vector4f PERU = new Vector4f(0.8f, 0.52f, 0.25f, 1);
    public static final Vector4f TOMATO = new Vector4f(1, 0.39f, 0.28f, 1);
    public static final Vector4f ROYAL_BLUE = new Vector4f(0.25f, 0.41f, 0.88f, 1);
}
