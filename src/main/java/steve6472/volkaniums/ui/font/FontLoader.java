package steve6472.volkaniums.ui.font;

import steve6472.volkaniums.Constants;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.util.ResourceCrawl;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Volkaniums <br>
 */
public class FontLoader
{
    private static final File FONTS = new File(Constants.RESOURCES_FOLDER, "font/font");

    public static FontEntry bootstrap()
    {
        List<FontEntry> fonts = new ArrayList<>();

        ResourceCrawl.crawlAndLoadJsonCodec(FONTS, Font.CODEC, (info, key) ->
        {
            FontEntry entry = new FontEntry(key, info, fonts.size());
            fonts.add(entry);
        });

        for (FontEntry font : fonts)
        {
            font.font().init(font.key());
            VolkaniumsRegistries.FONT.register(font);
        }

        return fonts.getFirst();
    }
}
