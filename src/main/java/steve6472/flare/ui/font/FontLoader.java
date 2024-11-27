package steve6472.flare.ui.font;

import com.mojang.datafixers.util.Pair;
import steve6472.core.registry.Key;
import steve6472.flare.core.Flare;
import steve6472.flare.module.Module;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.util.ResourceCrawl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public class FontLoader
{
    private static final String PATH = "font/fonts";

    public static void bootstrap()
    {
        List<Pair<FontEntry, Module>> fonts = new ArrayList<>();

        for (Module module : Flare.getModuleManager().getModules())
        {
            module.iterateNamespaces((folder, namespace) ->
                ResourceCrawl.crawlAndLoadJsonCodec(new File(folder, PATH), Font.CODEC, (info, key) ->
            {
                FontEntry entry = new FontEntry(Key.withNamespace(namespace, key), info, fonts.size());
                fonts.add(Pair.of(entry, module));
            }));
        }

        for (Pair<FontEntry, Module> pair : fonts)
        {
            FontEntry font = pair.getFirst();
            font.font().init(pair.getSecond(), font.key());
            FlareRegistries.FONT.register(font);
        }
    }
}
