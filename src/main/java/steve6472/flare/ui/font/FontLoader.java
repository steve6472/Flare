package steve6472.flare.ui.font;

import com.mojang.datafixers.util.Pair;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.flare.core.Flare;
import steve6472.flare.module.Module;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.util.ResourceCrawl;

import java.io.*;
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

    private static final String PATH = "font/fonts";

    public static void bootstrap()
    {
        Map<Key, Pair<FontEntry, Module>> fonts = new LinkedHashMap<>();

        for (Module module : Flare.getModuleManager().getModules())
        {
            module.iterateNamespaces((folder, namespace) ->
            {
                File file = new File(folder, PATH);

                ResourceCrawl.crawlAndLoadJsonCodec(file, Font.CODEC, (info, id) ->
                {
                    Key key = Key.withNamespace(namespace, id);
                    int index = fonts.containsKey(key) ? fonts.get(key).getFirst().index() : fonts.size();
                    FontEntry entry = new FontEntry(key, info, index);
                    LOGGER.finest("Loaded font " + entry.key() + " from " + module.name());
                    fonts.put(key, Pair.of(entry, module));
                });
            });
        }

        for (Pair<FontEntry, Module> pair : fonts.values())
        {
            FontEntry font = pair.getFirst();
            font.font().init(pair.getSecond(), font.key());
            FlareRegistries.FONT.register(font);
        }
    }
}
