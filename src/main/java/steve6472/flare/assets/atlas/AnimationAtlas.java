package steve6472.flare.assets.atlas;

import com.mojang.datafixers.util.Pair;
import steve6472.core.registry.Key;
import steve6472.core.util.ImagePacker;
import steve6472.flare.SamplerLoader;
import steve6472.flare.assets.Texture;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.settings.VisualSettings;
import steve6472.flare.ui.textures.SpriteEntry;
import steve6472.flare.util.Obj;
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
    public AnimationAtlas(Key key, Map<Key, Pair<SpriteEntry, BufferedImage>> animatedAtlasData, Obj<ImagePacker> packerGet)
    {
        setKey(key);

        Map<Key, BufferedImage> images = new HashMap<>(animatedAtlasData.size());
        animatedAtlasData.forEach((entryKey, pair) -> {
            registerSprite(entryKey, pair.getFirst());
            images.put(entryKey, pair.getSecond());
        });

        Map<String, BufferedImage> toPack = new HashMap<>();
        images.forEach((entryKey, image) -> toPack.put(entryKey.toString(), image));
        ImagePacker imagePacker = PackerUtil.pack(SpriteLoader.STARTING_IMAGE_SIZE, toPack, false);
        images.clear();

        if (VisualSettings.GENERATE_STARTUP_ATLAS_DATA.get())
        {
            SamplerLoader.Debug.generateFromAtlasAndImagePacker(SamplerLoader.Debug.getFile("/atlas_data"), this, imagePacker);
        }
        fixUvs(imagePacker);
        packerGet.set(imagePacker);
    }

    @Override
    void create(Map<Atlas, ImagePacker> packerMap)
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
