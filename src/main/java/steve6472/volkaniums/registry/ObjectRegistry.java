package steve6472.volkaniums.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by steve6472
 * Date: 5/4/2024
 * Project: Domin <br>
 */
public class ObjectRegistry<T extends Keyable>
{
    private final Key key;
    private final Map<Key, T> map = new HashMap<>();
    private final T defaultValue;

    public ObjectRegistry(Key key, T defaultValue)
    {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public ObjectRegistry(Key key)
    {
        this(key, null);
    }

    public Key getRegistryKey()
    {
        return key;
    }

    public Codec<Key> keyCodec()
    {
        return Key.CODEC;
    }

    public Codec<T> byKeyCodec()
    {
        return keyCodec().flatXmap(key -> {
            T t = map.get(key);
            if (t == null)
                return DataResult.error(() -> "No entry in registry '" + getRegistryKey() + "' for key '" + key + "'");
            else
                return DataResult.success(t);
        }, t -> DataResult.success(t.key()));
    }

    public void register(Key key, T obj)
    {
        map.put(key, obj);
    }

    public T register(T obj)
    {
        map.put(obj.key(), obj);
        return obj;
    }

    public T get(Key key)
    {
        return map.getOrDefault(key, defaultValue);
    }

    public Collection<Key> keys()
    {
        return Set.copyOf(map.keySet());
    }
}
