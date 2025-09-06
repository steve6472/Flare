package steve6472.flare.render.common;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.VkBuffer;
import steve6472.flare.descriptors.DescriptorWriter;

import static org.lwjgl.vulkan.VK10.VK_BUFFER_USAGE_STORAGE_BUFFER_BIT;
import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public record EntrySBO(int instanceSize, int memoryPropertyFlags, int stage) implements CommonEntry
{
    @Override
    public void write(DescriptorWriter writer, int index, MemoryStack stack, Object userObject)
    {
        writer.writeBuffer(index, stack, ((VkBuffer) userObject));
    }

    @Override
    public Object createObject(VkDevice device)
    {
        VkBuffer sbo = new VkBuffer(device, instanceSize, 1, VK_BUFFER_USAGE_STORAGE_BUFFER_BIT, memoryPropertyFlags);
        sbo.map();
        return sbo;
    }

    @Override
    public int type()
    {
        return VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;
    }

    @Override
    public int stage()
    {
        return stage;
    }
}
