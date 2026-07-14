package steve6472.flare.assets.atlas;

import com.mojang.datafixers.util.Pair;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Key;
import steve6472.flare.Commands;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.util.PackerUtil;

import java.awt.image.BufferedImage;
import java.util.*;

import static org.lwjgl.vulkan.VK10.VK_FILTER_NEAREST;
import static org.lwjgl.vulkan.VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST;

/**
 * Created by steve6472
 * Date: 7/18/2025
 * Project: Flare <br>
 */
public class AnimationAtlas extends Atlas
{
    public AnimationAtlas(Map<Key, Pair<SpriteEntry, BufferedImage>> animatedAtlasData)
    {
        Map<Key, BufferedImage> images = new HashMap<>(animatedAtlasData.size());
        animatedAtlasData.forEach((key, pair) -> {
            sprites.put(key, pair.getFirst());
            images.put(key, pair.getSecond());
        });

        Map<String, BufferedImage> toPack = new HashMap<>();
        images.forEach((key, image) -> toPack.put(key.toString(), image));
        this.imagePacker = PackerUtil.pack(SpriteLoader.STARTING_IMAGE_SIZE, toPack, false);
        images.clear();
        fixUvs(imagePacker);
    }

    @Override
    void create()
    {
    }

    @Override
    TextureSampler createVkResource(BufferedImage image, VkSetup setup)
    {
        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(setup.device(), image, setup.commands().commandPool, setup.graphicsQueue());
        return new TextureSampler(texture, setup.device(), key(), VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
    }

    @Override
    void mergeWith(Atlas other)
    {
        throw new UnsupportedOperationException("Animation atlas should never have to be merged, these are created after sprite atlas is merged");
    }
}
