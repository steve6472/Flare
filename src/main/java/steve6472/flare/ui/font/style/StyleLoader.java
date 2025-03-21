package steve6472.flare.ui.font.style;

import steve6472.core.log.Log;
import steve6472.core.module.ModuleUtil;
import steve6472.core.registry.Key;
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

    private static final String PATH = "font/styles";

    public static void bootstrap()
    {
        Map<Key, FontStyleEntry> styles = new LinkedHashMap<>();

        ModuleUtil.loadModuleJsonCodecsDebug(Flare.getModuleManager(), PATH, FontStyle.CODEC, LOGGER, "font style", (_, _, key, object) -> {
            int index = styles.containsKey(key) ? styles.get(key).index() : styles.size();
            FontStyleEntry entry = new FontStyleEntry(key, object, index);
            styles.put(key, entry);
        });

        styles.forEach(FlareRegistries.FONT_STYLE::register);

        FontEntry fontEntry = FlareRegistries.FONT.get(UnknownCharacter.FONT_KEY);
        FontStyleEntry fontStyle = FlareRegistries.FONT_STYLE.get(UnknownCharacter.STYLE_KEY);
        if (fontEntry == null || fontStyle == null)
        {
            LOGGER.severe("Font for unknown character not found! This is a bug!");
            LOGGER.severe("Please create font file 'unknown' and font style 'unknown' with single 'A' char that will be used as an error glyph.");
            LOGGER.severe("Until fixed, any unknown character will crash the program instead of displaying unknown symbol.");
        }
        else
        {
            UnknownCharacter.init();
        }
    }
}
