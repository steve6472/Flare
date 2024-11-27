package steve6472.flare.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import steve6472.core.log.Log;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/11/2024
 * Project: Flare <br>
 */
public final class ResourceCrawl
{
    private static final Logger LOGGER = Log.getLogger(ResourceCrawl.class);

    public static void crawl(File startingDir, boolean stripExtFromRel, BiConsumer<File, String> end)
    {
        recursiveCrawl(startingDir, startingDir, stripExtFromRel, end);
    }

    public static <T> void crawlAndLoadJsonCodec(File startingDir, Codec<T> codec, BiConsumer<T, String> end)
    {
        crawl(startingDir, true, (file, relPath) ->
        {
            InputStreamReader streamReader;
            try
            {
                streamReader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            } catch (FileNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            BufferedReader reader = new BufferedReader(streamReader);
            JsonElement jsonElement;
            try
            {
                jsonElement = JsonParser.parseReader(reader);
            } catch (JsonIOException | JsonSyntaxException e)
            {
                LOGGER.severe("Could not load json from '" + file + "'");
                throw new RuntimeException(e);
            }

            DataResult<Pair<T, JsonElement>> decode;
            try
            {
                decode = codec.decode(JsonOps.INSTANCE, jsonElement);
            } catch (Exception ex)
            {
                LOGGER.severe("Error when decoding:\n" + jsonElement.toString());
                throw ex;
            }

            relPath = relPath.replace("\\", "/");
            T obj = decode.getOrThrow().getFirst();
            end.accept(obj, relPath);
        });
    }

    private static void recursiveCrawl(File file, File startingDir, boolean stripExtFromRel, BiConsumer<File, String> end)
    {
        File[] files = file.listFiles();
        if (files == null)
            return;

        for (File listFile : files)
        {
            if (listFile.isDirectory())
                recursiveCrawl(listFile, startingDir, stripExtFromRel, end);
            else
            {
                String replace = listFile.getAbsolutePath().replace("\\", "/");
                String replace1 = startingDir.getAbsolutePath().replace("\\", "/");
                String substring = replace.substring(replace1.length() + 1);
                if (stripExtFromRel)
                    substring = substring.substring(0, substring.lastIndexOf('.'));
                end.accept(listFile, substring);
            }
        }
    }
}
