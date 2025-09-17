package steve6472.flare.render.common;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.VkBuffer;
import steve6472.flare.descriptors.DescriptorWriter;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public record EntryVertexBuffer(int instanceSize, int instanceCount, int memoryPropertyFlags) implements CommonEntry
{
    @Override
    public void write(DescriptorWriter writer, int index, MemoryStack stack, Object userObject)
    {
    }

    @Override
    public Object createObject(VkDevice device)
    {
        VkBuffer buffer = new VkBuffer(device, instanceSize, instanceCount, VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, memoryPropertyFlags);
        buffer.map();
        return buffer;
    }

    @Override
    public int type()
    {
        return 0;
    }

    @Override
    public int stage()
    {
        return 0;
    }
}
