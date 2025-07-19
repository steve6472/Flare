package steve6472.flare.assets.atlas;

import com.mojang.datafixers.util.Pair;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Key;
import steve6472.flare.Commands;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.ui.textures.SpriteEntry;

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
        tempImages = new HashMap<>();
        animatedAtlasData.forEach((key, pair) -> {
            sprites.put(key, pair.getFirst());
            tempImages.put(key, pair.getSecond());
        });
    }

    @Override
    void create()
    {

    }

    @Override
    void createVkResource(BufferedImage image, VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        Texture texture = new Texture();
        texture.createTextureImageFromBufferedImage(device, image, commands.commandPool, graphicsQueue);
        TextureSampler sampler = new TextureSampler(texture, device, key(), VK_FILTER_NEAREST, VK_SAMPLER_MIPMAP_MODE_NEAREST, false);
        FlareRegistries.SAMPLER.register(sampler);
        this.sampler = sampler;
    }

    @Override
    void mergeWith(Atlas other)
    {
        throw new UnsupportedOperationException("Animation atlas should never have to be merged, these are created after sprite atlas is merged");
    }
}
