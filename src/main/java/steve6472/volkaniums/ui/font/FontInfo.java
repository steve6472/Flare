package steve6472.volkaniums.ui.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.registry.VolkaniumsRegistries;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.ui.font.layout.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;

/**
 * Created by steve6472
 * Date: 10/19/2024
 * Project: Volkaniums <br>
 */
public class FontInfo
{
    private static final Logger LOGGER = Log.getLogger(FontInfo.class);

    private static final File GENERATOR = new File("resources/font/msdf-atlas-gen.exe");
    private static final File GENERATED_FONT_TEXTURE = new File("resources/font/generated/font.png");
    private static final File GENERATED_FONT_LAYOUT = new File("resources/font/generated/font_layout.json");
    private static final File GENERATED_CHARSET = new File("resources/font/generated/charset.txt");

    final Long2ObjectMap<GlyphInfo> glyphs = new Long2ObjectOpenHashMap<>(1024);
    final Long2ObjectMap<Long2FloatMap> kerning = new Long2ObjectOpenHashMap<>(1024);
    final GlyphInfo ERROR;
    private AtlasData atlasData;
    private Metrics metrics;

    public FontInfo()
    {
        LOGGER.finest("Init font");
        try
        {
            if (VisualSettings.GENERATE_FONT.get())
            {
                LOGGER.finest("Generating charset");
                generateCharset();
                LOGGER.finest("Calling msdf-atlas-gen");
                generateFontFiles(VisualSettings.FONT_PATH.get().replace("%SystemRoot%", System.getenv("SystemRoot")));
                LOGGER.finest("Deleting charset");
                deleteCharset();

                VisualSettings.GENERATE_FONT.set(false);
            }
            LOGGER.finest("Init font data");
            initFontData();
            ERROR = glyphs.get('X');

            LOGGER.finest("Updating atlas sizes for font styles");
            for (Key key : VolkaniumsRegistries.FONT_STYLE.keys())
            {
                VolkaniumsRegistries.FONT_STYLE.get(key).style().atlasSize().set(atlasData.width() / atlasData.size(), atlasData.height() / atlasData.size());
            }

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private void generateCharset()
    {
        String charset = "[0x0,0xFFFF]";
        try (FileWriter writer = new FileWriter(GENERATED_CHARSET))
        {
            writer.write(charset);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void deleteCharset()
    {
        if (GENERATED_CHARSET.exists() && !GENERATED_CHARSET.delete())
        {
            LOGGER.warning("Could not delete charset!");
        }
    }

    private void generateFontFiles(String fontFilePath) throws IOException, InterruptedException
    {
        Process process = new ProcessBuilder().command(
            GENERATOR.getPath(),
            "-font", fontFilePath,
            "-type", AtlasType.MTSDF.stringValue(),
            "-potr",
            "-size", "48",
            "-format", "png",
//            "-allglyphs",
            "-charset", GENERATED_CHARSET.getPath(),
            "-yorigin", "top",
            "-imageout", GENERATED_FONT_TEXTURE.getPath(),
            "-json", GENERATED_FONT_LAYOUT.getPath(),
            "-pxpadding", "1").start();

        consumeStream(process.getInputStream(), LOGGER::fine);
        consumeStream(process.getErrorStream(), LOGGER::warning);

        boolean finished = process.waitFor(VisualSettings.FONT_GENERATE_TIMEOUT.get(), TimeUnit.SECONDS);

        if (!finished)
        {
            process.destroy(); // Timeout exceeded; kill the process
            LOGGER.severe("Font generating took longer than allowed!");
        } else {
            LOGGER.finest("Font generating finished");
        }
    }

    private static void consumeStream(InputStream stream, Consumer<String> print)
    {
        new Thread(() ->
        {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (VisualSettings.FONT_GEN_LOGS.get())
                        print.accept(line);
                }
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }).start();
    }

    private void initFontData() throws FileNotFoundException
    {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(GENERATED_FONT_LAYOUT), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        JsonElement jsonElement = JsonParser.parseReader(reader);
        DataResult<Pair<FontMetrics, JsonElement>> decode = FontMetrics.CODEC.decode(JsonOps.INSTANCE, jsonElement);
        FontMetrics metrics = decode.getOrThrow().getFirst();
        this.atlasData = metrics.atlas();
        this.metrics = metrics.metrics();

        for (GlyphInfo glyph : metrics.glyphs())
        {
            glyphs.put(glyph.index(), glyph);
        }

        for (Kerning kerning : metrics.kernings())
        {
            this.kerning.computeIfAbsent(kerning.unicode1(), _ -> new Long2FloatLinkedOpenHashMap()).put(kerning.unicode2(), kerning.advance());
        }
    }

    public TextureSampler getSamplerLoader(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        BufferedImage fontTexture;
        try
        {
            fontTexture = ImageIO.read(GENERATED_FONT_TEXTURE);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, fontTexture, commands.commandPool, graphicsQueue);
        return new TextureSampler(texture, device, Constants.FONT_TEXTURE, VK_FILTER_LINEAR);
    }

    public AtlasData getAtlasData()
    {
        return atlasData;
    }

    public Metrics getMetrics()
    {
        return metrics;
    }
}
