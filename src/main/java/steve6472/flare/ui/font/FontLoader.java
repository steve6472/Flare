package steve6472.flare.ui.font;

import com.mojang.datafixers.util.Pair;
import steve6472.core.module.ModuleUtil;
import steve6472.core.registry.Key;
import steve6472.core.module.Module;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public class FontLoader
{
    public static void bootstrap()
    {
        Map<Key, Pair<FontEntry, Module>> fonts = new LinkedHashMap<>();

        ModuleUtil.loadModuleJsonCodecs(FlareParts.FONT, Flare.getModuleManager(), Font.CODEC, (module, _, key, object) -> {

            int index = fonts.containsKey(key) ? fonts.get(key).getFirst().index() : fonts.size();
            FontEntry entry = new FontEntry(key, object, index);
            fonts.put(key, Pair.of(entry, module));
        });

        for (Pair<FontEntry, Module> pair : fonts.values())
        {
            FontEntry font = pair.getFirst();
            font.font().init(pair.getSecond(), font.key());
            FlareRegistries.FONT.register(font);
        }
    }
}
