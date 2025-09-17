package steve6472.flare.assets.model.blockbench;

import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 9/10/2024
 * Project: Flare <br>
 */
public class SkinData
{
    public Map<UUID, Pair<Integer, Matrix4f>> transformations = new HashMap<>();

    public Matrix4f[] toArray()
    {
        Matrix4f[] arr = new Matrix4f[transformations.size()];

        for (Pair<Integer, Matrix4f> value : transformations.values())
        {
            arr[value.getFirst() - 1] = value.getSecond();
        }

        return arr;
    }

    public Matrix4f[] toArrayCopy()
    {
        Matrix4f[] arr = new Matrix4f[transformations.size()];

        for (Pair<Integer, Matrix4f> value : transformations.values())
        {
            arr[value.getFirst() - 1] = new Matrix4f(value.getSecond());
        }

        return arr;
    }

    public Matrix4f[] toArray(SkinData toBlend, float blendFactor)
    {
        if (toBlend.transformations.size() != transformations.size())
            throw new RuntimeException("Transformations size does not match somehow");

        Matrix4f[] arr = new Matrix4f[transformations.size()];

        transformations.forEach((key, value) ->
        {
            Matrix4f left = value.getSecond();
            Matrix4f right = toBlend.transformations.get(key).getSecond();
            arr[value.getFirst() - 1] = left.lerp(right, blendFactor, new Matrix4f());
        });

        return arr;
    }

    public SkinData copy()
    {
        SkinData newData = new SkinData();
        transformations.forEach((k, v) -> {
            newData.transformations.put(k, new Pair<>(v.getFirst(), new Matrix4f(v.getSecond())));
        });

        return newData;
    }
}
