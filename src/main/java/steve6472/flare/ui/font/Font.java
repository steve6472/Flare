package steve6472.flare.ui.font;

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
import steve6472.core.module.Module;
import steve6472.core.registry.Key;
import steve6472.flare.Commands;
import steve6472.flare.FlareConstants;
import steve6472.flare.SamplerLoader;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.ui.font.layout.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_LINEAR;

/**
 * Created by steve6472
 * Date: 10/19/2024
 * Project: Flare <br>
 */
public class Font
{
    private static final Logger LOGGER = Log.getLogger(Font.class);

    private static final String DEFAULT_CHARSET = "[0x0,0xFFFF]";
    private static final int DEFAULT_SIZE = 48;
    private static final int DEFAULT_PXPADDING = 1;

    public static final Codec<Font> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("path").forGetter(o -> o.fontPath),
        Codec.STRING.optionalFieldOf("charset", DEFAULT_CHARSET).forGetter(o -> o.charset),
        Codec.INT.optionalFieldOf("size", DEFAULT_SIZE).forGetter(o -> o.size),
        Codec.INT.optionalFieldOf("px_padding", DEFAULT_PXPADDING).forGetter(o -> o.pxPadding),
        AtlasType.CODEC.optionalFieldOf("type", AtlasType.MTSDF).forGetter(o -> o.type),
        Filtering.CODEC.optionalFieldOf("filtering", Filtering.LINEAR).forGetter(o -> o.filtering)
    ).apply(instance, Font::new));

    private final String charset;
    private final String fontPath;
    private final int size;
    private final int pxPadding;
    private final AtlasType type;
    private final Filtering filtering;

    private final Long2ObjectMap<GlyphInfo> glyphs = new Long2ObjectOpenHashMap<>(1024);
    private final Long2ObjectMap<Long2FloatMap> kerning = new Long2ObjectOpenHashMap<>(1024);
    private AtlasData atlasData;
    private Metrics metrics;

    private Font(String fontPath, String charset, int size, int pxPadding, AtlasType type, Filtering filtering)
    {
        this.fontPath = fontPath;
        this.charset = charset;
        this.size = size;
        this.pxPadding = pxPadding;
        this.type = type;
        this.filtering = filtering;
    }

    public void init(Module module, Key key)
    {
        String name = key.id();
        final File fontNamespace = new File(new File(FlareConstants.GENERATED_FLARE, "font"), key.namespace());

        final File fontTexture = new File(fontNamespace, name + ".png");
        final File fontLayout = new File(fontNamespace, name + "_layout.json");
        final File fontCharset = new File(fontNamespace, name + "_charset.txt");

        LOGGER.finest("Init font " + key);
        try
        {
            if (!fontTexture.exists() || !fontLayout.exists() || !fontCharset.exists())
            {
                LOGGER.fine("Reason to generate: Font Texture -> " + fontTexture.exists() + ", Font Layout -> " + fontLayout.exists() + ", Font Charset -> " + fontCharset.exists());

                if (!fontNamespace.exists())
                {
                    if (!fontNamespace.mkdirs())
                    {
                        throw new RuntimeException("Could not font namespace folder " + fontNamespace.getAbsolutePath());
                    }
                }

                LOGGER.finest("Generating charset for " + key);
                generateCharset(fontCharset);
                LOGGER.finest("Calling msdf-atlas-gen");
                generateImageAndLayout(module.getRootFolder(), key.namespace(), fontTexture, fontLayout, fontCharset);
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

    private void generateImageAndLayout(File moduleRoot, String namespace, File fontTexture, File fontLayout, File fontCharset) throws IOException, InterruptedException
    {
        File fontFile;
        if (fontPath.contains("%SystemRoot%"))
        {
            fontFile = new File(fontPath.replace("%SystemRoot%", System.getenv("SystemRoot")));
        } else
        {
            fontFile = new File(new File(moduleRoot, namespace), "font/assets/" + fontPath);
        }
        LOGGER.finer("Generating font from " + fontFile.getAbsolutePath());

        if (!fontFile.exists())
            throw new RuntimeException("Font file not found! " + fontFile.getAbsolutePath());

        Process process = new ProcessBuilder().command(
            FlareConstants.MSDF_EXE.getPath(),
            "-font", fontFile.getAbsolutePath(),
            "-type", type.stringValue(),
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
        return new TextureSampler(texture, device, key, filtering.vkCode, filtering.vkCodeMipmap, true);
    }

    /*
     * Font data getters and util methods
     */

    public AtlasData getAtlasData()
    {
        return atlasData;
    }

    public Metrics getMetrics()
    {
        return metrics;
    }

    public String getCharset()
    {
        return charset;
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
