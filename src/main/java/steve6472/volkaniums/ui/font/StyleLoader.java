package steve6472.volkaniums.ui.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
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

    private static final String STYLES_PATH = "resources" + File.separator + "font" + File.separator + "styles" + File.separator;

    public static FontStyleEntry bootstrap()
    {
        List<FontStyleEntry> styles = new ArrayList<>();
        styles.add(new FontStyleEntry(FontStyle.BASE_KEY, FontStyle.BASE, 0));

        ResourceCrawl.crawl(new File(STYLES_PATH), true, (file, relPath) -> {

            InputStreamReader streamReader;
            try
            {
                streamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            BufferedReader reader = new BufferedReader(streamReader);
            JsonElement jsonElement = JsonParser.parseReader(reader);
            DataResult<Pair<FontStyle, JsonElement>> decode = FontStyle.CODEC.decode(JsonOps.INSTANCE, jsonElement);

            relPath = relPath.replace("\\", "/");
            FontStyle style = decode.getOrThrow().getFirst();
            FontStyleEntry entry = new FontStyleEntry(Key.defaultNamespace(relPath), style, styles.size());
            styles.add(entry);
        });

        styles.forEach(VolkaniumsRegistries.FONT_STYLE::register);

        return styles.getFirst();
    }
}
