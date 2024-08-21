package steve6472.volkaniums;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkMappedMemoryRange;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.function.BiConsumer;

import static org.lwjgl.vulkan.VK13.*;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public class VkBuffer
{
    private final VkDevice device;
    private PointerBuffer mapped;
    private long buffer = VK_NULL_HANDLE;
    private long memory = VK_NULL_HANDLE;

    private final int bufferSize;
    private final int instanceCount;
    private final int instanceSize;
    private final int alignmentSize;
    private final int usageFlags;
    private final int memoryPropertyFlags;

    public VkBuffer(VkDevice device, int instanceSize, int instanceCount, int usageFlags, int memoryPropertyFlags, int minOffsetAlignment)
    {
        this.device = device;
        this.instanceSize = instanceSize;
        this.instanceCount = instanceCount;
        this.usageFlags = usageFlags;
        this.memoryPropertyFlags = memoryPropertyFlags;

        alignmentSize = getAlignment(instanceSize, minOffsetAlignment);
        bufferSize = alignmentSize * instanceCount;
        createBuffer();
    }

    public void cleanup()
    {
        unmap();
        vkDestroyBuffer(device, buffer, null);
        vkFreeMemory(device, memory, null);
    }

    private void createBuffer()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);

            VulkanUtil.createBuffer(device, bufferSize, usageFlags, memoryPropertyFlags, pBuffer, pBufferMemory);

            buffer = pBuffer.get(0);
            memory = pBufferMemory.get(0);
        }
    }

    public int map(MemoryStack stack, long size, long offset)
    {
        if (buffer == VK_NULL_HANDLE || memory == VK_NULL_HANDLE) {
            throw new IllegalStateException("Called map on buffer before create");
        }

        mapped = stack.mallocPointer(1);
        return vkMapMemory(device, memory, offset, size, 0, mapped);
    }

    public void unmap()
    {
        if (mapped != null)
        {
            vkUnmapMemory(device, memory);
            mapped = null;
        }
    }

    public <T> void writeToBuffer(BiConsumer<ByteBuffer, T[]> memcpy, T[] data, long size, int offset)
    {
        if (size >= Integer.MAX_VALUE)
            throw new RuntimeException("Buffer can not be this big... hjelp");

        ByteBuffer buff = mapped.getByteBuffer(offset, (int) size);
        memcpy.accept(buff, data);
    }

    public int flush(long size, long offset)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkMappedMemoryRange mappedRange = VkMappedMemoryRange.calloc(stack);
            mappedRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE);
            mappedRange.memory(memory);
            mappedRange.offset(offset);
            mappedRange.size(size);
            return vkFlushMappedMemoryRanges(device, mappedRange);
        }
    }

    public VkDescriptorBufferInfo descriptorInfo(MemoryStack stack, long size, long offset)
    {
        VkDescriptorBufferInfo bufferInfo = VkDescriptorBufferInfo.calloc(stack);
        bufferInfo.buffer(buffer);
        bufferInfo.offset(offset);
        bufferInfo.range(size);
        return bufferInfo;
    }

    public int invalidate(long size, long offset)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkMappedMemoryRange mappedRange = VkMappedMemoryRange.calloc(stack);
            mappedRange.sType(VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE);
            mappedRange.memory(memory);
            mappedRange.offset(offset);
            mappedRange.size(size);

            return vkInvalidateMappedMemoryRanges(device, mappedRange);
        }
    }

    public long getBuffer()
    {
        return buffer;
    }

    public PointerBuffer getMappedMemory()
    {
        return mapped;
    }

    public int getInstanceCount()
    {
        return instanceCount;
    }

    public int getInstanceSize()
    {
        return instanceSize;
    }

    public int getAlignmentSize()
    {
        return alignmentSize;
    }

    public int getUsageFlags()
    {
        return usageFlags;
    }

    public int getMemoryPropertyFlags()
    {
        return memoryPropertyFlags;
    }

    public int getBufferSize()
    {
        return bufferSize;
    }

    /**
     * Returns the minimum instance size required to be compatible with devices minOffsetAlignment
     *
     * @param instanceSize The size of an instance
     * @param minOffsetAlignment The minimum required alignment, in bytes, for the offset member (eg
     * minUniformBufferOffsetAlignment)
     *
     * @return VkResult of the buffer mapping call
     */
    public static int getAlignment(int instanceSize, int minOffsetAlignment)
    {
        if (minOffsetAlignment > 0)
            return (instanceSize + minOffsetAlignment - 1) & -minOffsetAlignment;
        return instanceSize;
    }

    /*
     * Methods with defaults
     */

    public int map(MemoryStack stack)
    {
        return map(stack, VK_WHOLE_SIZE, 0);
    }

    public <T> void writeToBuffer(BiConsumer<ByteBuffer, T[]> memcpy, T[] data)
    {
        writeToBuffer(memcpy, data, VK_WHOLE_SIZE, 0);
    }

    public int flush()
    {
        return flush(VK_WHOLE_SIZE, 0);
    }

    public VkDescriptorBufferInfo descriptorInfo(MemoryStack stack)
    {
        return descriptorInfo(stack, VK_WHOLE_SIZE, 0);
    }

    public int invalidate()
    {
        return invalidate(VK_WHOLE_SIZE, 0);
    }
}
