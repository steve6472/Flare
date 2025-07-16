package steve6472.flare.assets.atlas;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.util.GsonUtil;
import steve6472.core.util.ImagePacker;
import steve6472.flare.Commands;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.source.SourceResult;
import steve6472.flare.assets.model.blockbench.ErrorModel;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.textures.SpriteData;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.util.PackerUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public class SpriteLoader
{
    public static boolean SAVE_DEBUG_ATLASES = false;

    private static final Map<Atlas, Map<Key, BufferedImage>> TEMP_IMAGES = new HashMap<>();

    private static final Logger LOGGER = Log.getLogger(SpriteLoader.class);
    private static final int STARTING_IMAGE_SIZE = 64;

    private static final String[] EXTENSIONS = {".json5", ".json"};

    public static Map<Key, SpriteEntry> loadFromAtlas(Atlas atlas, Set<SourceResult> input)
    {
        final Map<Key, BufferedImage> images = new HashMap<>();
        final Map<Key, SpriteEntry> entries = new HashMap<>();

        input.forEach(pair ->
        {
            SpriteData spriteData = loadSpriteData(pair.file());
            BufferedImage image;
            try
            {
                image = ImageIO.read(pair.file());
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            SpriteEntry entry = new SpriteEntry(pair.key(), spriteData, new Vector4f(), new Vector2i(image.getWidth(), image.getHeight()), entries.size());

            images.put(pair.key(), image);
            entries.put(pair.key(), entry);
        });

        images.put(FlareConstants.ERROR_TEXTURE, ErrorModel.IMAGE);
        entries.put(FlareConstants.ERROR_TEXTURE, new SpriteEntry(FlareConstants.ERROR_TEXTURE, SpriteData.DEFAULT, new Vector4f(), new Vector2i(2, 2), entries.size()));

        if (TEMP_IMAGES.put(atlas, images) != null)
        {
            throw new RuntimeException("Atlas " + atlas + " was processed twice!");
        }

        return entries;
    }

    private static SpriteData loadSpriteData(File imageFile)
    {
        for (String extension : EXTENSIONS)
        {
            String optionPath = imageFile.getAbsolutePath();
            optionPath = optionPath.substring(0, optionPath.length() - ".png".length());
            optionPath += extension;

            File optionFile = new File(optionPath);
            if (optionFile.exists())
            {
                JsonElement jsonElement = GsonUtil.loadJson(optionFile);
                DataResult<Pair<SpriteData, JsonElement>> decode;
                try
                {
                    decode = SpriteData.CODEC.decode(JsonOps.INSTANCE, jsonElement);
                } catch (Exception ex)
                {
                    LOGGER.severe("Error when decoding:\n" + jsonElement.toString());
                    throw ex;
                }

                return decode.getOrThrow().getFirst();
            }
        }

        return SpriteData.DEFAULT;
    }

    public static ImagePacker createTexture(Atlas atlas, VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        Map<Key, BufferedImage> images = TEMP_IMAGES.remove(atlas);

        Map<String, BufferedImage> toPack = new HashMap<>();
        images.forEach((key, image) -> toPack.put(key.toString(), image));
        ImagePacker packer = PackerUtil.pack(STARTING_IMAGE_SIZE, toPack, false);
        images.clear();

        BufferedImage image = packer.getImage();
        saveDebugAtlas(atlas, image);
        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, image, commands.commandPool, graphicsQueue);
        TextureSampler sampler = new TextureSampler(texture, device, atlas.key(), VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
        FlareRegistries.SAMPLER.register(sampler);
        atlas.sampler = sampler;
        fixUvs(atlas, packer);
        return packer;
    }

    private static void fixUvs(Atlas atlas, ImagePacker packer)
    {
        float texel = 1f / packer.getImage().getWidth();

        Map<Key, SpriteEntry> sprites = atlas.getSprites();
        for (Key key : sprites.keySet())
        {
            SpriteEntry uiTextureEntry = sprites.get(key);
            Rectangle rectangle = packer.getRects().get(key.toString());
            Objects.requireNonNull(rectangle, "Texture data not found in ImagePacker, for " + key);
            uiTextureEntry.uv().set(
                (rectangle.x) * texel,
                (rectangle.y) * texel,
                (rectangle.x + rectangle.width) * texel,
                (rectangle.y + rectangle.height) * texel
            );
        }
    }

    private static void saveDebugAtlas(Atlas atlas, BufferedImage image)
    {
        if (!SAVE_DEBUG_ATLASES)
            return;

        try
        {
            File namespaceDir = new File(FlareConstants.FLARE_DEBUG_ATLAS, atlas.key().namespace());
            String folderPath = atlas.key().id();
            if (folderPath.contains("/"))
                folderPath = atlas.key().id().substring(0, folderPath.lastIndexOf('/'));
            File pathDir = new File(namespaceDir, folderPath);
            if (!pathDir.exists() && !pathDir.mkdirs())
                LOGGER.warning("Failed to create path " + pathDir.getAbsolutePath());
            String filePath = atlas.key().id();
            if (filePath.contains("/"))
                filePath = filePath.substring(atlas.key().id().lastIndexOf('/'));
            File output = new File(pathDir, filePath + ".png");
            ImageIO.write(image, "PNG", output);
        } catch (IOException e)
        {
            LOGGER.warning("Failed to save debug " + atlas.key() + ", exception: " + e.getMessage());
        }
    }
}