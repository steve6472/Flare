package steve6472.volkaniums.ui.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.ui.font.layout.AtlasData;
import steve6472.volkaniums.ui.font.style.FontStyle;
import steve6472.volkaniums.util.ResourceCrawl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Volkaniums <br>
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

        styles.forEach(VolkaniumsRegistries.FONT_STYLE::register);

        udpateAtlasSizes();

        return styles.getFirst();
    }

    private static void udpateAtlasSizes()
    {
        for (Key styleKey : VolkaniumsRegistries.FONT_STYLE.keys())
        {
            FontStyle style = VolkaniumsRegistries.FONT_STYLE.get(styleKey).style();
            Font font = style.font();
            if (font == null)
            {
                LOGGER.severe("Font Style '" + styleKey + "' references '" + style.font() + "' which has not been loaded!");
                continue;
            }

            AtlasData atlasData = font.getAtlasData();
            style.atlasSize().set(atlasData.width() / atlasData.size(), atlasData.height() / atlasData.size());
        }
    }
}
