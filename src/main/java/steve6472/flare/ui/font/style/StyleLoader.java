package steve6472.flare.ui.font.style;

import steve6472.core.log.Log;
import steve6472.flare.Constants;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.font.FontEntry;
import steve6472.flare.ui.font.UnknownCharacter;
import steve6472.flare.util.ResourceCrawl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public class StyleLoader
{
    private static final Logger LOGGER = Log.getLogger(StyleLoader.class);

    private static final File STYLES = new File(Constants.RESOURCES_FOLDER, "font/styles");

    public static FontStyleEntry bootstrap()
    {
        List<FontStyleEntry> styles = new ArrayList<>();

        ResourceCrawl.crawlAndLoadJsonCodec(STYLES, FontStyle.CODEC, (style, key) ->
        {
            FontStyleEntry entry = new FontStyleEntry(key, style, styles.size());
            LOGGER.finest("Loaded font style " + entry.key());
            styles.add(entry);
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

        return styles.getFirst();
    }
}
