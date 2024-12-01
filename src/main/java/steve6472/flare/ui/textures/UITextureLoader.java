package steve6472.flare.ui.textures;

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
import steve6472.core.util.Preconditions;
import steve6472.flare.Commands;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.core.Flare;
import steve6472.flare.module.Module;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.textures.type.StretchTexture;
import steve6472.flare.ui.textures.type.UITexture;
import steve6472.flare.util.PackerUtil;
import steve6472.flare.util.ResourceCrawl;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public class UITextureLoader
{
    private static final Logger LOGGER = Log.getLogger(UITextureLoader.class);
    private static final int STARTING_IMAGE_SIZE = 512;

    private static final File DEBUG_ATLAS = new File(FlareConstants.FLARE_DEBUG_FOLDER, "ui_texture_atlas.png");
    private static final Map<Key, BufferedImage> IMAGES = new HashMap<>();

    private static final String PATH = "textures/ui";
    private static final String[] EXTENSIONS = {".json5", ".json"};

    public static void bootstrap()
    {
        Map<Key, File> uiTextures = new LinkedHashMap<>();

        for (Module module : Flare.getModuleManager().getModules())
        {
            module.iterateNamespaces((folder, namespace) ->
            {
                File file = new File(folder, PATH);

                ResourceCrawl.crawl(file, true, (filePath, id) ->
                {
                    String fileName = filePath.getName();
                    if (!fileName.endsWith(".png"))
                        return;

                    Key key = Key.withNamespace(namespace, id);
                    LOGGER.finest("Loaded UI Texture " + key + " from " + module.name());
                    uiTextures.put(key, filePath);
                });
            });
        }

        UITextureEntry errorEntry = new UITextureEntry(FlareConstants.ERROR_TEXTURE, StretchTexture.instance(), new Vector4f(), new Vector2i(2, 2), FlareRegistries.UI_TEXTURE.keys().size());
        FlareRegistries.UI_TEXTURE.register(errorEntry);

        uiTextures.forEach((key, imageFile) ->
        {
            UITexture texture = loadUITexture(imageFile);
            BufferedImage image;
            try
            {
                image = ImageIO.read(imageFile);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            UITextureEntry entry = new UITextureEntry(key, texture, new Vector4f(), new Vector2i(image.getWidth(), image.getHeight()), FlareRegistries.UI_TEXTURE.keys().size());
            FlareRegistries.UI_TEXTURE.register(entry);

            IMAGES.put(key, image);
        });
    }

    private static UITexture loadUITexture(File imageFile)
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
                DataResult<Pair<UITexture, JsonElement>> decode;
                try
                {
                    decode = UITexture.CODEC.decode(JsonOps.INSTANCE, jsonElement);
                } catch (Exception ex)
                {
                    LOGGER.severe("Error when decoding:\n" + jsonElement.toString());
                    throw ex;
                }

                return decode.getOrThrow().getFirst();
            }
        }

        return StretchTexture.instance();
    }

    public static void createTexture(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        Map<String, BufferedImage> toPack = new HashMap<>();
        IMAGES.forEach((key, image) -> toPack.put(key.toString(), image));
        ImagePacker packer = PackerUtil.pack(STARTING_IMAGE_SIZE, toPack, true);
        IMAGES.clear();

        BufferedImage image = packer.getImage();
        saveDebugAtlas(image);
        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, image, commands.commandPool, graphicsQueue);
        TextureSampler sampler = new TextureSampler(texture, device, FlareConstants.UI_TEXTURE, VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
        FlareRegistries.SAMPLER.register(sampler);
        fixUvs(packer);
    }

    private static void fixUvs(ImagePacker packer)
    {
        float texel = 1f / packer.getImage().getWidth();

        Collection<Key> keys = FlareRegistries.UI_TEXTURE.keys();
        for (Key key : keys)
        {
            UITextureEntry uiTextureEntry = FlareRegistries.UI_TEXTURE.get(key);
            Rectangle rectangle = packer.getRects().get(key.toString());
            Preconditions.checkNotNull(rectangle, "Texture data not found in ImagePacker, for " + key);
            uiTextureEntry.uv().set(
                (rectangle.x) * texel,
                (rectangle.y) * texel,
                (rectangle.x + rectangle.width) * texel,
                (rectangle.y + rectangle.height) * texel
            );
        }
    }

    private static void saveDebugAtlas(BufferedImage image)
    {
        try
        {
            ImageIO.write(image, "PNG", DEBUG_ATLAS);
        } catch (IOException e)
        {
            LOGGER.warning("Failed to save debug " + DEBUG_ATLAS.getName() + ", exception: " + e.getMessage());
        }
    }
}