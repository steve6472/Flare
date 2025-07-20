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
import steve6472.flare.assets.atlas.source.SourceResult;
import steve6472.flare.assets.model.blockbench.ErrorModel;
import steve6472.flare.ui.textures.SpriteData;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.ui.textures.animation.SpriteAnimation;
import steve6472.flare.util.Obj;
import steve6472.flare.util.PackerUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public class SpriteLoader
{
    private static final Logger LOGGER = Log.getLogger(SpriteLoader.class);
    public static boolean SAVE_DEBUG_ATLASES = false;

    private static final String[] EXTENSIONS = {".json5", ".json"};
    private static final int STARTING_IMAGE_SIZE = 64;

    public record LoadResult(Map<Key, Pair<SpriteEntry, BufferedImage>> entries, AnimationAtlas animationAtlas) {}

    public static LoadResult loadFromAtlas(Set<SourceResult> input)
    {
        final Map<Key, Pair<SpriteEntry, BufferedImage>> atlasData = new HashMap<>();
        final Map<Key, Pair<SpriteEntry, BufferedImage>> animatedAtlasData = new HashMap<>();

        input.forEach(pair -> processInput(pair, atlasData, animatedAtlasData));

        // Insert error image
        atlasData.put(FlareConstants.ERROR_TEXTURE,
            Pair.of(new SpriteEntry(FlareConstants.ERROR_TEXTURE, SpriteData.DEFAULT, new Vector4f(), new Vector2i(2, 2), atlasData.size()), ErrorModel.IMAGE));

        AnimationAtlas animationAtlas = null;
        if (!animatedAtlasData.isEmpty())
        {
            // Insert error image
            animatedAtlasData.put(FlareConstants.ERROR_TEXTURE,
                Pair.of(new SpriteEntry(FlareConstants.ERROR_TEXTURE, SpriteData.DEFAULT, new Vector4f(), new Vector2i(2, 2), animatedAtlasData.size()), ErrorModel.IMAGE));

            animationAtlas = new AnimationAtlas(animatedAtlasData);
        }

        return new LoadResult(atlasData, animationAtlas);
    }

    private static void processInput(SourceResult input, Map<Key, Pair<SpriteEntry, BufferedImage>> atlasData, Map<Key, Pair<SpriteEntry, BufferedImage>> animatedAtlasData)
    {
        Obj<Boolean> animationFailed = Obj.of(false);
        Obj<BufferedImage> image = Obj.empty();
        SpriteData spriteData = loadSpriteData(input.file());
        try
        {
            image.set(ImageIO.read(input.file()));
        } catch (IOException e)
        {
            LOGGER.severe("Failed to load image from " + input.file());
            throw new RuntimeException(e);
        }
        Key key = input.key();

        // Verify sizes, if they do not match animation size, do not add to atlas
        // Crop the whole texture into just one frame
        // Add animation data to the atlas
        spriteData.animation().ifPresent(animation ->
        {
            if (image.get().getWidth() % animation.width() != 0)
            {
                LOGGER.warning("Animated Sprite '%s' failed to load, image width (%s) is not a multiple of animation width (%s)".formatted(key, image.get().getWidth(), animation.width()));
                animationFailed.set(true);
            }

            if (image.get().getHeight() % animation.height() != 0)
            {
                LOGGER.warning("Animated Sprite '%s' failed to load, image height (%s) is not a multiple of animation height (%s)".formatted(key, image.get().getHeight(), animation.height()));
                animationFailed.set(true);
            }

            if (animationFailed.get())
                return;

            SpriteEntry animationEntry = new SpriteEntry(key, spriteData, new Vector4f(), new Vector2i(image.get().getWidth(), image.get().getHeight()), animatedAtlasData.size());
            animatedAtlasData.put(key, Pair.of(animationEntry, image.get()));

            image.set(cropAnimation(image.get(), animation));
        });

        // Animation failed, skip adding to atlas. Error texture will be used instead.
        if (animationFailed.get())
            return;

        SpriteEntry entry = new SpriteEntry(key, spriteData, new Vector4f(), new Vector2i(image.get().getWidth(), image.get().getHeight()), atlasData.size());

        atlasData.put(key, Pair.of(entry, image.get()));
    }

    private static BufferedImage cropAnimation(BufferedImage image, SpriteAnimation animation)
    {
        BufferedImage cropped = new BufferedImage(animation.width(), animation.height(), BufferedImage.TYPE_4BYTE_ABGR);

        for (int i = 0; i < animation.width(); i++)
        {
            for (int j = 0; j < animation.height(); j++)
            {
                cropped.setRGB(i, j, image.getRGB(i, j));
            }
        }

        return cropped;
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
        Map<Key, BufferedImage> images = atlas.tempImages;
        atlas.tempImages = null;

        Map<String, BufferedImage> toPack = new HashMap<>();
        images.forEach((key, image) -> toPack.put(key.toString(), image));
        ImagePacker packer = PackerUtil.pack(STARTING_IMAGE_SIZE, toPack, false);
        images.clear();

        BufferedImage image = packer.getImage();
        saveDebugAtlas(atlas, image);
        atlas.createVkResource(image, device, commands, graphicsQueue);
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
            else
                folderPath = "";
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