package steve6472.flare.ui.font;

import com.mojang.datafixers.util.Pair;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.module.Module;
import steve6472.core.registry.Registry;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.tracy.FlareProfiler;
import steve6472.flare.tracy.Profiler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public class FontLoader
{
    private static final Logger LOGGER = Log.getLogger(FontLoader.class);

    public static void bootstrap(Registry<FontEntry> registry)
    {
        Profiler profiler = FlareProfiler.frame();
        profiler.push("font");
        Map<Key, Pair<FontEntry, Module>> fonts = new LinkedHashMap<>();

        Flare.getModuleManager().loadModuleJsonCodecs(FlareParts.FONT, Font.CODEC, (module, _, key, object) -> {

            int index = fonts.containsKey(key) ? fonts.get(key).getFirst().index() : fonts.size();
            FontEntry entry = new FontEntry(key, object, index);
            fonts.put(key, Pair.of(entry, module));
        }, (ex, key) -> Log.exception(LOGGER, ex, "Failed to load Font '" + key + "'"));

        for (Pair<FontEntry, Module> pair : fonts.values())
        {
            FontEntry font = pair.getFirst();
            font.font().init(pair.getSecond(), font.key());
            Registry.register(registry, font.key(), font);
        }
        profiler.pop();
    }
}
