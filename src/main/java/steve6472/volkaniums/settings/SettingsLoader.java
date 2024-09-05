package steve6472.volkaniums.settings;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.registry.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 9/6/2024
 * Project: Volkaniums <br>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SettingsLoader
{
    public static JsonObject saveSettings()
    {
        Map theMap = new HashMap<>();

        for (Key key : Registries.SETTINGS.keys())
        {
            Settings.Setting<?, ?> setting = Registries.SETTINGS.get(key);
            theMap.put(setting, setting);
        }

        return (JsonObject) Codec.dispatchedMap(Registries.SETTINGS.byKeyCodec(), Serializable::codec).encodeStart(JsonOps.INSTANCE, theMap).getOrThrow();
    }

    public static void loadSettings(JsonObject json)
    {
        Map<Settings.Setting<?, ?>, ?> map = Codec
            .dispatchedMap(Registries.SETTINGS.byKeyCodec(), Serializable::codec)
            .decode(JsonOps.INSTANCE, json)
            .getOrThrow()
            .getFirst();

        map.forEach((k, v) -> {
            Settings.Setting<?, Object> setting = (Settings.Setting<?, Object>) Registries.SETTINGS.get(k.key());
            Settings.Setting<?, ?> v1 = (Settings.Setting<?, ?>) v;
            Object obj = v1.get();
            setting.set(obj);
        });
    }
}
