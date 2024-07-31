package steve6472.volkaniums.registry;

import com.mojang.serialization.Codec;

/**
 * Created by steve6472
 * Date: 5/4/2024
 * Project: Domin <br>
 */
public interface Serializable<T>
{
    Codec<T> codec();
}
