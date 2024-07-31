package steve6472.volkaniums.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by steve6472
 * Date: 5/4/2024
 * Project: Domin <br>
 */
public interface StringValue
{
    String stringValue();

    static <T extends StringValue> Codec<T> fromValues(Supplier<T[]> values)
    {
        T[] valueArray = values.get();
        Map<String, T> byStringLookup = new HashMap<>();

        for (T value : valueArray)
        {
            byStringLookup.put(value.stringValue(), value);
        }

        return Codec.STRING.flatXmap(string -> {
            T val = byStringLookup.get(string);
            if (val == null)
                return DataResult.error(() -> "No StringValue found for '" + string + "'");
            else
                return DataResult.success(val);
        }, t -> DataResult.success(t.stringValue()));
    }
}
