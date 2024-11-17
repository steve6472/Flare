package steve6472.flare.util;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Created by steve6472
 * Date: 11/13/2024
 * Project: Flare <br>
 */
public final class MatrixAnim
{
    public static float getAnimTime(long start, long end, long current)
    {
        return Math.min(1.0f, (float)(current - start) / (end - start));
    }

    public static Matrix4f animate(Matrix4f from, Matrix4f to, float t, Matrix4f store)
    {
        if (from.equals(to))
            return store.set(from);

        Vector3f translationA = new Vector3f(), translationB = new Vector3f();
        Quaternionf rotationA = new Quaternionf(), rotationB = new Quaternionf();
        Vector3f scaleA = new Vector3f(), scaleB = new Vector3f();

        decompose(from, translationA, rotationA, scaleA);
        decompose(to, translationB, rotationB, scaleB);

        Vector3f interpolatedTranslation = new Vector3f();
        translationA.lerp(translationB, t, interpolatedTranslation);

        Quaternionf interpolatedRotation = new Quaternionf();
        rotationA.slerp(rotationB, t, interpolatedRotation);

        Vector3f interpolatedScale = new Vector3f();
        scaleA.lerp(scaleB, t, interpolatedScale);

        return store
            .identity()
            .translate(interpolatedTranslation)
            .rotate(interpolatedRotation)
            .scale(interpolatedScale);
    }

    private static void decompose(Matrix4f mat, Vector3f translation, Quaternionf rotation, Vector3f scale)
    {
        mat.getTranslation(translation);
        mat.getUnnormalizedRotation(rotation);
        mat.getScale(scale);
    }
}
