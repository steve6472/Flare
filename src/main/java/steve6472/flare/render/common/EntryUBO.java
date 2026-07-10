package steve6472.flare.render.common;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.VkBuffer;
import steve6472.flare.descriptors.DescriptorWriter;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/10/2026
 * Project: Flare <br>
 *
 */
public record EntryUBO(int instanceSize, int memoryPropertyFlags, int stage, int rangeOverride) implements CommonEntry
{
    @Override
    public void write(DescriptorWriter writer, int index, MemoryStack stack, Object userObject)
    {
        writer.writeBuffer(index, stack, (VkBuffer) userObject, rangeOverride == -1 ? instanceSize : rangeOverride);
    }

    @Override
    public Object createObject(VkDevice device)
    {
        VkBuffer ubo = new VkBuffer(device, instanceSize, 1, VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, memoryPropertyFlags);
        ubo.map();
        return ubo;
    }

    @Override
    public int type()
    {
        return VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC;
    }

    @Override
    public int stage()
    {
        return stage;
    }
}
