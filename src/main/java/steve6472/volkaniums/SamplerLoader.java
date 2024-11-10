package steve6472.volkaniums;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.assets.model.blockbench.BlockbenchLoader;
import steve6472.volkaniums.registry.VkContent;
import steve6472.volkaniums.registry.VolkaniumsRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 10/20/2024
 * Project: Volkaniums <br>
 *
 */
public final class SamplerLoader
{
    private SamplerLoader() {}

    private static final List<VkContent<TextureSampler>> SAMPLER_LOADERS = new ArrayList<>();

    public static TextureSampler loadSamplers(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        TextureSampler bbSampler = BlockbenchLoader.packImages(device, commands, graphicsQueue);

        SAMPLER_LOADERS.forEach(loader ->
        {
            TextureSampler sampler = loader.apply(device, commands, graphicsQueue);
            VolkaniumsRegistries.SAMPLER.register(sampler);
        });

        return bbSampler;
    }

    public static void addSamplerLoader(VkContent<TextureSampler> loader)
    {
        SAMPLER_LOADERS.add(loader);
    }
}
