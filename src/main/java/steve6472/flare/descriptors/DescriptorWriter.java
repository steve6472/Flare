package steve6472.flare.descriptors;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.core.util.Preconditions;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.TextureSampler;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/22/2024
 * Project: Flare <br>
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
        writes.add(new Write(getDescriptorType(binding), binding, bufferInfo, null, 1));
        return this;
    }

    public DescriptorWriter writeBuffer(int binding, MemoryStack stack, VkBuffer buffer)
    {
        VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
        bufferInfo.offset(0);
        bufferInfo.range(buffer.getInstanceSize());
        bufferInfo.buffer(buffer.getBuffer());

        return writeBuffer(binding, bufferInfo);
    }

    public DescriptorWriter writeBuffer(int binding, MemoryStack stack, VkBuffer buffer, int rangeOverride)
    {
        VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.calloc(1, stack);
        bufferInfo.offset(0);
        bufferInfo.range(rangeOverride);
        bufferInfo.buffer(buffer.getBuffer());

        return writeBuffer(binding, bufferInfo);
    }

    public DescriptorWriter writeImage(int binding, VkDescriptorImageInfo.Buffer bufferInfo)
    {
        writes.add(new Write(getDescriptorType(binding), binding, null, bufferInfo, 1));
        return this;
    }

    public DescriptorWriter writeImages(int binding, VkDescriptorImageInfo.Buffer bufferInfo, int count)
    {
        writes.add(new Write(getDescriptorType(binding), binding, null, bufferInfo, count));
        return this;
    }

    public DescriptorWriter writeImage(int binding, MemoryStack stack, TextureSampler textureSampler)
    {
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
        imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
        imageInfo.imageView(textureSampler.textureImageView);
        imageInfo.sampler(textureSampler.textureSampler);

        return writeImage(binding, imageInfo);
    }

    public DescriptorWriter writeImages(int binding, MemoryStack stack, TextureSampler... textureSamplers)
    {
        if (textureSamplers.length == 0)
            throw new RuntimeException("Tried to create descriptor of 0 images");

        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(textureSamplers.length, stack);
        for (int i = 0; i < textureSamplers.length; i++)
        {
            VkDescriptorImageInfo info = imageInfo.get(i);
            info.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            info.imageView(textureSamplers[i].textureImageView);
            info.sampler(textureSamplers[i].textureSampler);
        }

        return writeImages(binding, imageInfo, textureSamplers.length);
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

    private record Write(int type, int binding, VkDescriptorBufferInfo.Buffer bufferInfo, VkDescriptorImageInfo.Buffer imageInfo,
                         int count)
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
            write.descriptorCount(count);
            if (bufferInfo != null)
                write.pBufferInfo(bufferInfo);
            else
                write.pImageInfo(imageInfo);
        }
    }
}
