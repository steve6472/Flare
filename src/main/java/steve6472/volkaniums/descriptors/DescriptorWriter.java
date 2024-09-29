package steve6472.volkaniums.descriptors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.volkaniums.VkBuffer;
import steve6472.volkaniums.assets.TextureSampler;
import steve6472.volkaniums.util.Preconditions;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/22/2024
 * Project: Volkaniums <br>
 */
public class DescriptorWriter
{
    private final List<Write> writes = new ArrayList<>();
    private final DescriptorSetLayout setLayout;
    private final DescriptorPool pool;

    public DescriptorWriter(DescriptorSetLayout setLayout, DescriptorPool pool)
    {
        this.setLayout = setLayout;
        this.pool = pool;
    }

    public DescriptorWriter writeBuffer(int binding, VkDescriptorBufferInfo.Buffer bufferInfo)
    {
        writes.add(new Write(getDescriptorType(binding), binding, bufferInfo, null));
        return this;
    }

    public DescriptorWriter writeBuffer(int binding, VkBuffer buffer, MemoryStack stack)
    {
        VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
        bufferInfo.offset(0);
        bufferInfo.range(buffer.getInstanceSize());
        bufferInfo.buffer(buffer.getBuffer());

        return writeBuffer(binding, bufferInfo);
    }

    public DescriptorWriter writeBuffer(int binding, VkBuffer buffer, MemoryStack stack, int rangeOverride)
    {
        VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
        bufferInfo.offset(0);
        bufferInfo.range(rangeOverride);
        bufferInfo.buffer(buffer.getBuffer());

        return writeBuffer(binding, bufferInfo);
    }

    public DescriptorWriter writeImage(int binding, VkDescriptorImageInfo.Buffer bufferInfo)
    {
        writes.add(new Write(getDescriptorType(binding), binding, null, bufferInfo));
        return this;
    }

    public DescriptorWriter writeImage(int binding, TextureSampler textureSampler, MemoryStack stack)
    {
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(textureSampler.textureImageView);
        imageInfo.sampler(textureSampler.textureSampler);

        return writeImage(binding, imageInfo);
    }

    private int getDescriptorType(int binding)
    {
        VkDescriptorSetLayoutBinding setBinding = setLayout.bindings.get(binding);
        Preconditions.checkNotNull(setBinding, "Binding " + binding + " is not created!");
        return setBinding.descriptorType();
    }

    private VkWriteDescriptorSet.Buffer createWriteBuffers(MemoryStack stack)
    {
        VkWriteDescriptorSet.Buffer writeBuffer = VkWriteDescriptorSet.calloc(writes.size(), stack);
        for (int i = 0; i < writes.size(); i++)
        {
            writes.get(i).createSet(writeBuffer.get(i));
        }

        return writeBuffer;
    }

    public long build()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer descriptorSetLayout = stack.longs(setLayout.descriptorSetLayout);
            LongBuffer pDescriptorSet = stack.callocLong(1);
            boolean success = pool.allocateDescriptor(descriptorSetLayout, pDescriptorSet);

            if (!success)
                throw new RuntimeException("Failed to allocate descriptor sets");

            long descriptorSet = pDescriptorSet.get(0);

            override(descriptorSet, stack);
            return descriptorSet;
        }
    }

    private void override(long descriptorSet, MemoryStack stack)
    {
        VkWriteDescriptorSet.Buffer writeBuffer = createWriteBuffers(stack);

        for (int i = 0; i < writes.size(); i++)
        {
            VkWriteDescriptorSet write = writeBuffer.get(i);
            write.dstSet(descriptorSet);
        }

        vkUpdateDescriptorSets(pool.device, writeBuffer, null);
    }

    private record Write(int type, int binding, VkDescriptorBufferInfo.Buffer bufferInfo, VkDescriptorImageInfo.Buffer imageInfo)
    {
        public Write
        {
            if (bufferInfo != null && imageInfo != null)
            {
                throw new RuntimeException("Write can have only one type!");
            }
        }

        void createSet(VkWriteDescriptorSet write)
        {
            write.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            write.dstBinding(binding);
            write.dstArrayElement(0);
            write.descriptorType(type);
            write.descriptorCount(1);
            if (bufferInfo != null)
                write.pBufferInfo(bufferInfo);
            else
                write.pImageInfo(imageInfo);
        }
    }
}
