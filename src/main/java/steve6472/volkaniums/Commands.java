package steve6472.volkaniums;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK13.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class Commands
{
    public long commandPool;
    public List<VkCommandBuffer> commandBuffers;

    public void createCommandPool(VkDevice device, long surface)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            QueueFamilyIndices queueFamilyIndices = QueueFamilyIndices.findQueueFamilies(device.getPhysicalDevice(), surface);

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);

            LongBuffer pCommandPool = stack.mallocLong(1);

            if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.COMMAND_POOL_CREATION.format());
            }

            commandPool = pCommandPool.get(0);
        }
    }

    public void createCommandBuffers(VkDevice device, SwapChain swapChain, GraphicsPipeline graphicsPipeline, Model model)
    {
        final int commandBuffersCount = swapChain.swapChainFramebuffers.size();

        commandBuffers = new ArrayList<>(commandBuffersCount);

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.commandPool(commandPool);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandBufferCount(commandBuffersCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);

            if (vkAllocateCommandBuffers(device, allocInfo, pCommandBuffers) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.COMMAND_BUFFER_ALLOCATION.format());
            }

            for (int i = 0; i < commandBuffersCount; i++)
            {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device));
            }
        }
    }

    public static VkRenderPassBeginInfo createRenderPass(MemoryStack stack, GraphicsPipeline graphicsPipeline, SwapChain swapChain)
    {
        VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
        renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
        renderPassInfo.renderPass(graphicsPipeline.renderPass);
        VkRect2D renderArea = VkRect2D.calloc(stack);
        renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
        renderArea.extent(swapChain.swapChainExtent);
        renderPassInfo.renderArea(renderArea);
        VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
        clearValues.color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
        renderPassInfo.pClearValues(clearValues);

        return renderPassInfo;
    }

    public void freeCommandBuffers(VkDevice device)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            vkFreeCommandBuffers(device, commandPool, VulkanUtil.asPointerBuffer(stack, commandBuffers));
        }
        commandBuffers.clear();
    }
}
