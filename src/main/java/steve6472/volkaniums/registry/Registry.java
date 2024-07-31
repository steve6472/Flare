package steve6472.volkaniums.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 5/4/2024
 * Project: Domin <br>
 */
public class Registry<T extends Keyable & Serializable<?>>
{
    private final Key key;
    private final Map<Key, T> map = new HashMap<>();

    public Registry(Key key)
    {
        this.key = key;
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
        }, t ->
        {
            return DataResult.success(t.key());
        });
    }

    public Codec<Map<T, Object>> valueMapCodec()
    {
        return Codec.dispatchedMap(byKeyCodec(), Serializable::codec);
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
        return map.get(key);
    }

    public Map<Key, T> getMap()
    {
        return map;
    }
}
