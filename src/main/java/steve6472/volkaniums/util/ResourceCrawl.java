package steve6472.volkaniums.util;

import java.io.File;
import java.util.function.BiConsumer;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Volkaniums <br>
 */
public final class ResourceCrawl
{
    public record CrawlSettings(File startingDir, boolean stripExtFromRel, BiConsumer<File, String> end)
    {

    }

    public static void crawl(CrawlSettings settings)
    {
        recursiveCrawl(settings.startingDir(), settings);
    }

    public static void crawl(File startingDir, boolean stripExtFromRel, BiConsumer<File, String> end)
    {
        crawl(new CrawlSettings(startingDir, stripExtFromRel, end));
    }

    private static void recursiveCrawl(File file, CrawlSettings settings)
    {
        File[] files = file.listFiles();
        if (files == null)
            return;

        for (File listFile : files)
        {
            if (listFile.isDirectory())
                recursiveCrawl(listFile, settings);
            else
            {
                String replace = listFile.getAbsolutePath().replace("\\", "/");
                String replace1 = settings.startingDir().getAbsolutePath().replace("\\", "/");
                String substring = replace.substring(replace1.length() + 1);
                if (settings.stripExtFromRel)
                    substring = substring.substring(0, substring.lastIndexOf('.'));
                settings.end().accept(listFile, substring);
            }
        }
    }
}
