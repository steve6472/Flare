package steve6472.volkaniums.ui.font;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import steve6472.volkaniums.SamplerLoader;
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
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
public class Font
{
    private static final Logger LOGGER = Log.getLogger(Font.class);

    private static final String DEFAULT_CHARSET = "[0x0,0xFFFF]";
    private static final int DEFAULT_SIZE = 48;
    private static final int DEFAULT_PXPADDING = 1;
    private static final File GENERATED_FONT = new File(Constants.GENERATED_FOLDER, "font");
    private static final File GENERATOR = new File(Constants.RESOURCES_FOLDER, "font/generator/msdf-atlas-gen.exe");

    public static final Codec<Font> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.flatXmap(fontPath ->
        {
            fontPath = fontPath.replace("%SystemRoot%", System.getenv("SystemRoot"));
            return DataResult.success(new File(fontPath));
        }, _ -> DataResult.error(() -> "Too lazy to serialize, so just don't")).fieldOf("path").forGetter(o -> o.fontFile),
        Codec.STRING.optionalFieldOf("charset", DEFAULT_CHARSET).forGetter(o -> o.charset),
        Codec.INT.optionalFieldOf("size", DEFAULT_SIZE).forGetter(o -> o.size),
        Codec.INT.optionalFieldOf("px_padding", DEFAULT_PXPADDING).forGetter(o -> o.pxPadding)
    ).apply(instance, Font::new));

    private final String charset;
    private final File fontFile;
    private final int size;
    private final int pxPadding;

    private final Long2ObjectMap<GlyphInfo> glyphs = new Long2ObjectOpenHashMap<>(1024);
    private final Long2ObjectMap<Long2FloatMap> kerning = new Long2ObjectOpenHashMap<>(1024);
    private AtlasData atlasData;
    private Metrics metrics;

    private Font(File fontFile, String charset, int size, int pxPadding)
    {
        this.fontFile = fontFile;
        this.charset = charset;
        this.size = size;
        this.pxPadding = pxPadding;
    }

    public void init(Key key)
    {
        String name = key.id();
        final File fontTexture = new File(GENERATED_FONT, name + ".png");
        final File fontLayout = new File(GENERATED_FONT, name + "_layout.json");
        final File fontCharset = new File(GENERATED_FONT, name + "_charset.txt");

        LOGGER.finest("Init font " + key);
        try
        {
            if (!fontTexture.exists() || !fontLayout.exists() || !fontCharset.exists())
            {
                LOGGER.fine("Reason to generate: fontTexture.exists() -> " + !fontTexture.exists() + ", fontLayout.exists() -> " + !fontLayout.exists() + ", fontCharset.exists() -> " + !fontCharset.exists());

                File parentFile = fontTexture.getParentFile();
                if (!parentFile.exists())
                {
                    if (!parentFile.mkdirs())
                    {
                        throw new RuntimeException("Could not create " + parentFile.getAbsolutePath());
                    }
                }

                LOGGER.finest("Generating charset for " + key);
                generateCharset(fontCharset);
                LOGGER.finest("Calling msdf-atlas-gen");
                generateImageAndLayout(fontTexture, fontLayout, fontCharset);
            }
            readFontLayout(fontLayout);

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        SamplerLoader.addSamplerLoader((device, commands, queue) -> getSamplerLoader(device, commands, queue, fontTexture, key));
    }

    private void generateCharset(File fontCharset)
    {
        try (FileWriter writer = new FileWriter(fontCharset))
        {
            writer.write(charset);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void generateImageAndLayout(File fontTexture, File fontLayout, File fontCharset) throws IOException, InterruptedException
    {
        LOGGER.finer("Generating font from " + fontFile.getAbsolutePath());

        Process process = new ProcessBuilder().command(
            GENERATOR.getPath(),
            "-font", fontFile.getAbsolutePath(),
            "-type", AtlasType.MTSDF.stringValue(),
            "-potr",
            "-size", Integer.toString(size),
            "-format", "png",
            "-charset", fontCharset.getAbsolutePath(),
            "-yorigin", "top",
            "-imageout", fontTexture.getAbsolutePath(),
            "-json", fontLayout.getAbsolutePath(),
            "-pxpadding", Integer.toString(pxPadding)).start();

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
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
            }
        }).start();
    }

    private void readFontLayout(File fontLayout) throws FileNotFoundException
    {
        InputStreamReader streamReader = new InputStreamReader(new FileInputStream(fontLayout), StandardCharsets.UTF_8);
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

    private TextureSampler getSamplerLoader(VkDevice device, Commands commands, VkQueue graphicsQueue, File fontFile, Key key)
    {
        BufferedImage fontTexture;
        try
        {
            fontTexture = ImageIO.read(fontFile);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, fontTexture, commands.commandPool, graphicsQueue);
        return new TextureSampler(texture, device, key, VK_FILTER_LINEAR);
    }

    public AtlasData getAtlasData()
    {
        return atlasData;
    }

    public Metrics getMetrics()
    {
        return metrics;
    }

    public Long2ObjectMap<Long2FloatMap> getKerning()
    {
        return kerning;
    }

    public String getCharset()
    {
        return charset;
    }

    public Long2ObjectMap<GlyphInfo> getGlyphs()
    {
        return glyphs;
    }

    public GlyphInfo glyphInfo(long character)
    {
        GlyphInfo glyphInfo = glyphs.get(character);
        if (glyphInfo == null)
            return UnknownCharacter.unknownGlyph();
        return glyphInfo;
    }

    public float kerningAdvance(char left, char right)
    {
        Long2FloatMap leftKern = kerning.get(left);
        if (leftKern == null)
            return 0f;

        return leftKern.get(right);
    }

    public float getWidth(char c, float size)
    {
        GlyphInfo glyphInfo = glyphInfo(c);
        return glyphInfo.advance() * size;
    }

    public float getHeight(char c, float size)
    {
        GlyphInfo glyphInfo = glyphInfo(c);
        return glyphInfo.planeBounds().height() * size;
    }

    public float getWidth(String text, float size)
    {
        float width = 0;

        for (char c : text.toCharArray())
        {
            GlyphInfo glyphInfo = glyphInfo(c);
            width += glyphInfo.advance() * size;
        }

        return width;
    }

    public float getWidth(char[] chars, float size)
    {
        float width = 0;

        for (char c : chars)
        {
            GlyphInfo glyphInfo = glyphInfo(c);
            width += glyphInfo.advance() * size;
        }

        return width;
    }

    public float getMaxHeight(String text, float size)
    {
        float height = 0;

        for (char c : text.toCharArray())
        {
            GlyphInfo glyphInfo = glyphInfo(c);
            height = Math.max(height, glyphInfo.planeBounds().height() * size);
        }

        return height;
    }

    public float getMaxHeight(char[] chars, float size)
    {
        float height = 0;

        for (char c : chars)
        {
            GlyphInfo glyphInfo = glyphInfo(c);
            height = Math.max(height, glyphInfo.planeBounds().height() * size);
        }

        return height;
    }
}
