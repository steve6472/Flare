package steve6472.volkaniums.descriptors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET;

/**
 * Created by steve6472
 * Date: 8/22/2024
 * Project: Volkaniums <br>
 */
public class DescriptorWriter
{
    private List<VkWriteDescriptorSet> writes = new ArrayList<>();
    private DescriptorSetLayout setLayout;
    private DescriptorPool pool;

    public DescriptorWriter(DescriptorSetLayout setLayout, DescriptorPool pool)
    {
        this.setLayout = setLayout;
        this.pool = pool;
    }

    public DescriptorWriter writeBuffer(int binding, VkDescriptorBufferInfo.Buffer bufferInfo)
    {
        VkDescriptorSetLayoutBinding bindingDescription = setLayout.bindings.get(binding);

        VkWriteDescriptorSet write = VkWriteDescriptorSet.malloc();
        write.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        write.descriptorType(bindingDescription.descriptorType());
        write.dstBinding(binding);
        write.pBufferInfo(bufferInfo);
        write.descriptorCount(1);
        writes.add(write);

        return this;
    }

    public VkWriteDescriptorSet createWriteSet(int binding, VkDescriptorBufferInfo.Buffer bufferInfo)
    {
        VkDescriptorSetLayoutBinding bindingDescription = setLayout.bindings.get(binding);

        VkWriteDescriptorSet write = VkWriteDescriptorSet.malloc();
        write.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
        write.descriptorType(bindingDescription.descriptorType());
        write.dstBinding(binding);
        write.pBufferInfo(bufferInfo);
        write.descriptorCount(1);
        writes.add(write);

        return write;
    }

    public DescriptorWriter writeImage(int binding, VkDescriptorImageInfo.Buffer bufferInfo)
    {
        return this;
    }

    public boolean build(LongBuffer layouts, LongBuffer set)
    {
        boolean success = pool.allocateDescriptor(layouts, set);
        if (!success)
            return false;
        override(set.get(0));
        return true;
    }

    void override(long descriptorSet)
    {
        for (VkWriteDescriptorSet write : writes)
        {
            write.dstSet(descriptorSet);
        }
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkWriteDescriptorSet.Buffer writesBuffer = VkWriteDescriptorSet.calloc(writes.size(), stack);

            for (int i = 0; i < writes.size(); i++)
            {
                writesBuffer.put(i, writes.get(i));
            }

            VK13.vkUpdateDescriptorSets(pool.device, writesBuffer, null);
        }
    }
}
