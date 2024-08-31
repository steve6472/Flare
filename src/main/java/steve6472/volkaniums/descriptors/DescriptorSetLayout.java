package steve6472.volkaniums.descriptors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetLayoutBinding;
import org.lwjgl.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/22/2024
 * Project: Volkaniums <br>
 */
public class DescriptorSetLayout
{
    VkDevice device;
    public final long descriptorSetLayout;
    public Map<Integer, VkDescriptorSetLayoutBinding> bindings = new HashMap<>();

    private DescriptorSetLayout(VkDevice device, Map<Integer, LayoutBinding> bindings)
    {
        this.device = device;
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            for (int i = 0; i < bindings.size(); i++)
            {
                LayoutBinding layoutBinding = bindings.get(i);

                VkDescriptorSetLayoutBinding binding = VkDescriptorSetLayoutBinding.malloc();
                binding.binding(layoutBinding.binding());
                binding.descriptorType(layoutBinding.type());
                binding.descriptorCount(layoutBinding.count());
                binding.stageFlags(layoutBinding.flags());

                this.bindings.put(i, binding);
            }

            VkDescriptorSetLayoutBinding.Buffer layoutBindings = VkDescriptorSetLayoutBinding.calloc(bindings.size(), stack);

            this.bindings.forEach(layoutBindings::put);

            VkDescriptorSetLayoutCreateInfo descriptorSetLayoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
            descriptorSetLayoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            descriptorSetLayoutInfo.pBindings(layoutBindings);

            LongBuffer pSetLayout = stack.mallocLong(1);

            if (vkCreateDescriptorSetLayout(device, descriptorSetLayoutInfo, null, pSetLayout) != VK_SUCCESS)
            {
                throw new RuntimeException("Failed to create descriptor set layout");
            }

            descriptorSetLayout = pSetLayout.get();
        }
    }

    public void cleanup()
    {
        vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);
        bindings.forEach((k, v) ->
        {

            v.free();
        });
    }

    public static Builder builder(VkDevice device)
    {
        return new Builder(device);
    }

    public static final class Builder
    {
        VkDevice device;
        Map<Integer, LayoutBinding> bindings;

        private Builder(VkDevice device)
        {
            this.device = device;
            this.bindings = new HashMap<>();
        }

        public Builder addBinding(int binding, int descriptorType, int stageFlags, int count)
        {
            LayoutBinding layoutBinding = new LayoutBinding(binding, descriptorType, stageFlags, count);
            bindings.put(binding, layoutBinding);
            return this;
        }

        public Builder addBinding(int binding, int descriptorType, int stageFlags)
        {
            return addBinding(binding, descriptorType, stageFlags, 1);
        }

        public DescriptorSetLayout build()
        {
            return new DescriptorSetLayout(device, bindings);
        }
    }

    private record LayoutBinding(int binding, int type, int flags, int count) {}
}
