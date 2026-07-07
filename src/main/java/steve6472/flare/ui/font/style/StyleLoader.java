package steve6472.flare.ui.font.style;

import steve6472.core.registry.Key;
import steve6472.core.registry.Registry;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.ui.font.UnknownCharacter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public class StyleLoader
{
    public static void bootstrap(Registry<FontStyleEntry> registry)
    {
        Map<Key, FontStyleEntry> styles = new LinkedHashMap<>();

        Flare.getModuleManager().loadModuleJsonCodecs(FlareParts.STYLE, FontStyle.CODEC, (_, _, key, object) -> {
            int index = styles.containsKey(key) ? styles.get(key).index() : styles.size();
            FontStyleEntry entry = new FontStyleEntry(key, object, index);
            styles.put(key, entry);
        });

        styles.forEach((key, style) -> Registry.register(registry, key, style));
    }
}
