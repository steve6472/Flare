package steve6472.volkaniums;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public final class VulkanUtil
{
    private VulkanUtil() {}

    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    public static PointerBuffer asPointerBuffer(MemoryStack stack, Collection<String> collection)
    {
        PointerBuffer buffer = stack.mallocPointer(collection.size());

        collection.stream().map(stack::UTF8).forEach(buffer::put);

        return buffer.rewind();
    }

    public static PointerBuffer asPointerBuffer(MemoryStack stack, List<? extends Pointer> list)
    {
        PointerBuffer buffer = stack.mallocPointer(list.size());

        list.forEach(buffer::put);

        return buffer.rewind();
    }

    public static void createBuffer(VkDevice device, long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(size);
            bufferInfo.usage(usage);
            bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            if (vkCreateBuffer(device, bufferInfo, null, pBuffer) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.VERTEX_BUFFER_CREATION.format());
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
            vkGetBufferMemoryRequirements(device, pBuffer.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(findMemoryType(stack, device.getPhysicalDevice(), memRequirements.memoryTypeBits(), properties));

            if (vkAllocateMemory(device, allocInfo, null, pBufferMemory) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.VERTEX_BUFFER_ALLOCATION.format());
            }

            vkBindBufferMemory(device, pBuffer.get(0), pBufferMemory.get(0), 0);
        }
    }

    public static void memcpy(ByteBuffer buffer, VkVertex[] vertices)
    {
        for (VkVertex vertex : vertices)
        {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
        }
    }

    private static int findMemoryType(MemoryStack stack, VkPhysicalDevice physicalDevice, int typeFilter, int properties)
    {
        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.malloc(stack);
        vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);

        for (int i = 0; i < memProperties.memoryTypeCount(); i++)
        {
            if ((typeFilter & (1 << i)) != 0 && (memProperties
                .memoryTypes(i)
                .propertyFlags() & properties) == properties)
            {
                return i;
            }
        }

        throw new RuntimeException(ErrorCode.FIND_MEMORY_TYPE.format());
    }

    public static void copyBuffer(Commands commands, VkDevice device, VkQueue graphicsQueue, long srcBuffer, long dstBuffer, long size)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commands.commandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkBeginCommandBuffer(commandBuffer, beginInfo);
            {
                VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
                copyRegion.size(size);
                vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);
            }
            vkEndCommandBuffer(commandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(pCommandBuffer);

            if (vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.SUBMIT_COPY_COMMAND_BUFFER.format());
            }

            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffers(device, commands.commandPool, pCommandBuffer);
        }
    }
}
