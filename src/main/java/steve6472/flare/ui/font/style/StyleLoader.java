package steve6472.flare.ui.font.style;

import steve6472.core.log.Log;
import steve6472.core.module.ModuleUtil;
import steve6472.core.registry.Key;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.UnknownCharacter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public class StyleLoader
{
    private static final Logger LOGGER = Log.getLogger(StyleLoader.class);

    public static void bootstrap()
    {
        Map<Key, FontStyleEntry> styles = new LinkedHashMap<>();

        ModuleUtil.loadModuleJsonCodecs(FlareParts.STYLE, Flare.getModuleManager(), FontStyle.CODEC, (_, _, key, object) -> {
            int index = styles.containsKey(key) ? styles.get(key).index() : styles.size();
            FontStyleEntry entry = new FontStyleEntry(key, object, index);
            styles.put(key, entry);
        });

        styles.forEach(FlareRegistries.FONT_STYLE::register);
        UnknownCharacter.init();
    }
}
