package steve6472.flare.render.common;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.descriptors.DescriptorWriter;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_FRAGMENT_BIT;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public record EntrySampler(TextureSampler textureSampler) implements CommonEntry
{
    @Override
    public void write(DescriptorWriter writer, int index, MemoryStack stack, Object userObject)
    {
        writer.writeImage(index, stack, ((TextureSampler) userObject));
    }

    @Override
    public Object createObject(VkDevice device)
    {
        return textureSampler;
    }

    @Override
    public int type()
    {
        return VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
    }

    @Override
    public int stage()
    {
        return VK_SHADER_STAGE_FRAGMENT_BIT;
    }
}
