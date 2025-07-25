package steve6472.flare;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.registry.Key;
import steve6472.core.util.ImagePacker;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.SpriteAtlas;
import steve6472.flare.assets.model.blockbench.BlockbenchLoader;
import steve6472.flare.registry.VkContent;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.assets.atlas.SpriteLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Flare <br>
 *
 */
public final class SamplerLoader
{
    private SamplerLoader() {}

    private static final List<VkContent<TextureSampler>> SAMPLER_LOADERS = new ArrayList<>();

    public static TextureSampler loadSamplers(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        for (Key key : FlareRegistries.ATLAS.keys())
        {
            Atlas atlas = FlareRegistries.ATLAS.get(key);
            ImagePacker packer = SpriteLoader.createTexture(atlas, device, commands, graphicsQueue);
            if (atlas.key().equals(FlareConstants.ATLAS_BLOCKBENCH))
            {
                BlockbenchLoader.fixModelUvs(packer);
            }
        }

        SAMPLER_LOADERS.forEach(loader ->
        {
            TextureSampler sampler = loader.apply(device, commands, graphicsQueue);
            FlareRegistries.SAMPLER.register(sampler);
        });

        return null;
    }

    public static void addSamplerLoader(VkContent<TextureSampler> loader)
    {
        SAMPLER_LOADERS.add(loader);
    }
}
