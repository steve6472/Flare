package steve6472.flare.assets.atlas;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.joml.Vector2i;
import org.joml.Vector4f;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.util.GsonUtil;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.atlas.source.SourceResult;
import steve6472.flare.assets.model.blockbench.ErrorModel;
import steve6472.flare.ui.textures.SpriteData;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.ui.textures.animation.SpriteAnimation;
import steve6472.flare.util.Obj;

import javax.imageio.ImageIO;
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

    private static final String[] EXTENSIONS = {".json5", ".json"};
    public static final int STARTING_IMAGE_SIZE = 64;

    public record LoadResult(Map<Key, Pair<SpriteEntry, BufferedImage>> entries, Map<Key, Pair<SpriteEntry, BufferedImage>> animatedAtlasData) {}

    public static LoadResult loadFromAtlas(Set<SourceResult> input)
    {
        final Map<Key, Pair<SpriteEntry, BufferedImage>> atlasData = new HashMap<>();
        final Map<Key, Pair<SpriteEntry, BufferedImage>> animatedAtlasData = new HashMap<>();

        input.forEach(pair -> processInput(pair, atlasData, animatedAtlasData));

        // Insert error image
        atlasData.put(FlareConstants.ERROR_TEXTURE,
            Pair.of(new SpriteEntry(FlareConstants.ERROR_TEXTURE, SpriteData.DEFAULT, new Vector4f(), new Vector2i(2, 2), atlasData.size()), ErrorModel.IMAGE));

        if (!animatedAtlasData.isEmpty())
        {
            // Insert error image
            animatedAtlasData.put(FlareConstants.ERROR_TEXTURE,
                Pair.of(new SpriteEntry(FlareConstants.ERROR_TEXTURE, SpriteData.DEFAULT, new Vector4f(), new Vector2i(2, 2), animatedAtlasData.size()), ErrorModel.IMAGE));
        }

        return new LoadResult(atlasData, animatedAtlasData);
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
}