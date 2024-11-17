package steve6472.flare.descriptors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/22/2024
 * Project: Flare <br>
 */
public class DescriptorPool
{
    VkDevice device;
    public final long descriptorPool;

    private DescriptorPool(VkDevice device, int maxSets, int poolFlags, List<DescriptorPoolSize> poolSizesList)
    {
        this.device = device;

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.calloc(poolSizesList.size(), stack);

            for (int i = 0; i < poolSizesList.size(); i++)
            {
                DescriptorPoolSize descriptorPoolSize = poolSizesList.get(i);
                VkDescriptorPoolSize poolSize = poolSizes.get(i);
                poolSize.type(descriptorPoolSize.descriptorType());
                poolSize.descriptorCount(descriptorPoolSize.count());
            }

            VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolInfo.pPoolSizes(poolSizes);
            poolInfo.maxSets(maxSets);
            poolInfo.flags(poolFlags);

            LongBuffer pDescriptorPool = stack.mallocLong(1);

            if (vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool) != VK_SUCCESS)
            {
                throw new RuntimeException("Failed to create descriptor pool!");
            }

            descriptorPool = pDescriptorPool.get(0);
        }
    }

    public void cleanup()
    {
        vkDestroyDescriptorPool(device, descriptorPool, null);
    }

    public boolean allocateDescriptor(LongBuffer descriptorSetLayout, LongBuffer descriptor)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool(descriptorPool);
            allocInfo.pSetLayouts(descriptorSetLayout);

            return vkAllocateDescriptorSets(device, allocInfo, descriptor) == VK_SUCCESS;
        }
    }

    public void freeDescriptors(long[] descriptors)
    {
        vkFreeDescriptorSets(device, descriptorPool, descriptors);
    }

    public void resetPool()
    {
        vkResetDescriptorPool(device, descriptorPool, 0);
    }

    public static Builder builder(VkDevice device)
    {
        return new Builder(device);
    }

    public static final class Builder
    {
        VkDevice device;
        List<DescriptorPoolSize> poolSizes;
        int maxSets = 1000;
        int poolFlags = 0;

        private Builder(VkDevice device)
        {
            this.device = device;
            this.poolSizes = new ArrayList<>();
        }

        public Builder addPoolSize(int descriptorType, int count)
        {
            DescriptorPoolSize poolSize = new DescriptorPoolSize(descriptorType, count);
            poolSizes.add(poolSize);
            return this;
        }

        public Builder setPoolFlags(int flags)
        {
            poolFlags = flags;
            return this;
        }

        public Builder setMaxSets(int count)
        {
            maxSets = count;
            return this;
        }

        public DescriptorPool build()
        {
            return new DescriptorPool(device, maxSets, poolFlags, poolSizes);
        }
    }

    private record DescriptorPoolSize(int descriptorType, int count) {}
}
