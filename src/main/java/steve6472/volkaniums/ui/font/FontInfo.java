package steve6472.volkaniums.ui.font;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.*;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.util.ColorUtil;
import steve6472.core.util.ImagePacker;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.assets.Texture;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.settings.VisualSettings;
import steve6472.volkaniums.util.PackerUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.lwjgl.util.freetype.FreeType.*;
import static org.lwjgl.vulkan.VK10.VK_FILTER_LINEAR;

/**
 * Created by steve6472
 * Date: 10/19/2024
 * Project: Volkaniums <br>
 */
public class FontInfo
{
    private static final Logger LOGGER = Log.getLogger(FontInfo.class);

    final Long2ObjectMap<GlyphInfo> glyphs = new Long2ObjectOpenHashMap<>(1024);
    final GlyphInfo ERROR;

    private ByteBuffer fontBuffer;
    private FT_Face face;
    private long library;

    public FontInfo()
    {
        initFreeType(VisualSettings.FONT_PATH.get().replace("%SystemRoot%", System.getenv("SystemRoot")));
        ERROR = loadChar(65);
    }

    public void initFreeType(String fontFilePath)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // Initialize FreeType library
            PointerBuffer pointerBuffer = stack.mallocPointer(1);
            int err = FT_Init_FreeType(pointerBuffer);
            if (err != 0)
                throw new RuntimeException("Failed to initialize FreeType, err: " + FreeType.FT_Error_String(err) + " (0x" + Integer.toHexString(err) + ")");
            library = pointerBuffer.get();

            byte[] bytes = Files.readAllBytes(new File(fontFilePath).toPath());
            fontBuffer = MemoryUtil.memAlloc(bytes.length);
            fontBuffer.put(bytes).flip();

            pointerBuffer = stack.mallocPointer(1);
            err = FT_New_Memory_Face(library, fontBuffer, 0, pointerBuffer);
            if (err != 0)
                throw new RuntimeException("Failed to create font face, err: " + FreeType.FT_Error_String(err) + " (0x" + Integer.toHexString(err) + ")");
            face = FT_Face.create(pointerBuffer.get());

            FT_Set_Pixel_Sizes(face, 0, VisualSettings.FONT_QUALITY.get());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public TextureSampler getSamplerLoader(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ImagePacker imagePacker = loadChars();

        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, imagePacker.getImage(), commands.commandPool, graphicsQueue);
        return new TextureSampler(texture, device, Constants.FONT_TEXTURE, VK_FILTER_LINEAR);
    }

    private GlyphInfo loadChar(long code)
    {
        int err = FT_Load_Char(face, code, FT_LOAD_RENDER);
        if (err != 0)
        {
            LOGGER.severe("Could not load char " + code + ", err: " + FreeType.FT_Error_String(err) + " (0x" + Integer.toHexString(err) + ")");
            return ERROR;
        }

        FT_GlyphSlot glyph = face.glyph();

        FT_Render_Glyph(glyph, FT_RENDER_MODE_SDF);

        FT_Bitmap bitmap = glyph.bitmap();
        FT_Vector advance = glyph.advance();

        GlyphInfo glyphInfo = new GlyphInfo(
            new Vector2i(bitmap.width(), bitmap.rows()),
            new Vector2i(glyph.bitmap_left(), glyph.bitmap_top()),
            (int) advance.x(),
            new Vector4f()
        );
        glyphs.put(code, glyphInfo);

        return glyphInfo;
    }

    public ImagePacker loadChars()
    {
        Map<String, BufferedImage> images = new HashMap<>();
        // Start from space
        for (int i = ' '; i < 128; i++)
        {
            GlyphInfo glyphInfo = loadChar(i);

            if (i == ' ')
                continue;

            if (glyphInfo.isInvisible())
            {
                LOGGER.warning("Skipping char " + i + " '" + (char) i + "' has no size");
                continue;
            }

            FT_GlyphSlot glyph = face.glyph();
            FT_Bitmap bitmap = glyph.bitmap();

            ByteBuffer buffer = bitmap.buffer(bitmap.width() * bitmap.rows());
            BufferedImage image = new BufferedImage(bitmap.width(), bitmap.rows(), BufferedImage.TYPE_BYTE_GRAY);
            for (int j = 0; j < bitmap.rows(); j++)
            {
                for (int k = 0; k < bitmap.width(); k++)
                {
                    image.setRGB(k, j, ColorUtil.getColor(buffer.get()));
                }
            }

            images.put("" + i, image);
        }

        ImagePacker pack = PackerUtil.pack(512, images, true);

        pack.getRects().forEach((key, rect) ->
        {
            float texel = 1f / pack.getImage().getWidth();
            GlyphInfo glyphInfo;

            if (key.equals(Constants.ERROR_TEXTURE))
                glyphInfo = ERROR;
            else
                glyphInfo = glyphs.get(Long.parseLong(key));

            glyphInfo.texturePos().set(
                rect.x * texel,
                rect.y * texel,
                (rect.x + rect.width) * texel,
                (rect.y + rect.height) * texel);
        });

        saveDebugImage(pack.getImage());
        return pack;
    }

    private static void saveDebugImage(BufferedImage image)
    {
        try
        {
            ImageIO.write(image, "PNG", new File("font.png"));
        } catch (IOException e)
        {
            LOGGER.warning("Failed to save debug font.png, exception: " + e.getMessage());
        }
    }

    public void cleanup()
    {
        MemoryUtil.memFree(fontBuffer);
        FT_Done_Face(face);
        FT_Done_FreeType(library);
    }
}
