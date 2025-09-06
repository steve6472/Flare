package steve6472.flare.render.common;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.descriptors.DescriptorWriter;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public interface CommonEntry
{
    int type();
    int stage();

    void write(DescriptorWriter writer, int index, MemoryStack stack, Object userObject);

    Object createObject(VkDevice device);
}
