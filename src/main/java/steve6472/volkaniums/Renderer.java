package steve6472.volkaniums;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public class Renderer
{

    private final Window window;
    private final VkDevice device;
    private final VkQueue graphicsQueue;
    private final VkQueue presentQueue;
    private final long surface;

    private SwapChain swapChain;
    private GraphicsPipeline graphicsPipeline;
    private Commands commands;

    Model model;

    private Frame thisFrame;
    private int currentImageIndex;

    private boolean isFrameStarted;

    public Renderer(Window window, VkDevice device, VkQueue graphicsQueue, VkQueue presentQueue, long surface)
    {
        this.window = window;
        this.device = device;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
        this.surface = surface;

        swapChain = new SwapChain();
        graphicsPipeline = new GraphicsPipeline();
        commands = new Commands();
        commands.createCommandPool(device, surface);

        // Create models here
        model = new Model();
        model.createVertexBuffer(device, commands, graphicsQueue);

        createSwapChainObjects();
    }

    private void createSwapChainObjects()
    {
        swapChain.createSwapChain(surface, device, window.window());
        swapChain.createImageViews(device);
        graphicsPipeline.createRenderPass(device, swapChain);
        graphicsPipeline.createGraphicsPipeline(device, swapChain);
        swapChain.createFrameBuffers(device, graphicsPipeline.renderPass);
        commands.createCommandBuffers(device, swapChain, graphicsPipeline, model);
        swapChain.createSyncObjects(device);
    }

    private void recreateSwapChain()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width = stack.ints(0);
            IntBuffer height = stack.ints(0);

            while (width.get(0) == 0 && height.get(0) == 0)
            {
                glfwGetFramebufferSize(window.window(), width, height);
                glfwWaitEvents();
            }
        }
        vkDeviceWaitIdle(device);

        cleanupSwapChain();
        createSwapChainObjects();
    }

    private void cleanupSwapChain()
    {
        swapChain.swapChainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));

        commands.freeCommandBuffers(device);

        vkDestroyPipeline(device, graphicsPipeline.graphicsPipeline, null);
        vkDestroyPipelineLayout(device, graphicsPipeline.pipelineLayout, null);
        vkDestroyRenderPass(device, graphicsPipeline.renderPass, null);
        swapChain.swapChainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        vkDestroySwapchainKHR(device, swapChain.swapChain, null);
        swapChain.cleanup(device);
    }

    public void cleanup()
    {
        cleanupSwapChain();

        model.destroy(device);

        vkDestroyCommandPool(device, commands.commandPool, null);
    }

    VkCommandBuffer beginFrame(MemoryStack stack, IntBuffer pImageIndex)
    {
        if (isFrameStarted)
            throw new RuntimeException("Can't call beginFrame while already in progress");

        thisFrame = swapChain.inFlightFrames.get(swapChain.currentFrame);

        int vkResult = swapChain.acquireNextImage(device, pImageIndex, thisFrame);
        currentImageIndex = pImageIndex.get(0);

        if (vkResult == VK_ERROR_OUT_OF_DATE_KHR)
        {
            recreateSwapChain();
            return null;
        } else if (vkResult != VK_SUCCESS)
        {
            throw new RuntimeException("Cannot get image");
        }

        isFrameStarted = true;

        if (swapChain.imagesInFlight.containsKey(currentImageIndex))
            vkWaitForFences(device, swapChain.imagesInFlight.get(currentImageIndex).fence(), true, VulkanUtil.UINT64_MAX);

        swapChain.imagesInFlight.put(currentImageIndex, thisFrame);

        VkCommandBuffer commandBuffer = getCurrentCommandBuffer();

        VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
        beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

        if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS)
        {
            throw new RuntimeException(ErrorCode.BEGIN_COMMAND_RECORDING.format());
        }

        return commandBuffer;
    }

    void endFrame(MemoryStack stack, IntBuffer pImageIndex)
    {
        if (!isFrameStarted)
            throw new RuntimeException("Can't call endFrame while frame is not in progress");

        VkCommandBuffer commandBuffer = getCurrentCommandBuffer();

        if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS)
        {
            throw new RuntimeException(ErrorCode.END_COMMAND_RECORDING.format());
        }

        int vkResult = swapChain.submitCommandBuffers(device, graphicsQueue, presentQueue, commandBuffer, pImageIndex, stack, thisFrame);

        if (vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR || window.isFramebufferResize())
        {
            window.resetFramebufferResizeFlag();
            recreateSwapChain();
        } else if (vkResult != VK_SUCCESS)
        {
            throw new RuntimeException("Failed to present swap chain image");
        }

        isFrameStarted = false;
    }

    void beginSwapChainRenderPass(VkCommandBuffer commandBuffer, MemoryStack stack)
    {
        if (!isFrameStarted)
            throw new RuntimeException("Can't call beginSwapChainRenderPass while already in progress");

        if (commandBuffer != getCurrentCommandBuffer())
            throw new RuntimeException("Can't begin render pass on command buffer from a different frame");

        VkRenderPassBeginInfo renderPassInfo = Commands.createRenderPass(stack, graphicsPipeline, swapChain);

        renderPassInfo.framebuffer(swapChain.swapChainFramebuffers.get(currentImageIndex));

        vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
    }

    public void recordCommandBuffer(VkCommandBuffer commandBuffer, MemoryStack stack)
    {
        graphicsPipeline.bind(commandBuffer);

        for (int j = 0; j < 4; j++)
        {
            ByteBuffer buff = stack.calloc(28);
            Vector2f offset = new Vector2f(0.0f + (float) Math.sin(Math.toRadians((System.currentTimeMillis() % 3600) / 10d)), -0.4f * j * 0.25f);
            Vector3f color = new Vector3f(0.0f, 0.0f, 0.2f + 0.2f * j);

            offset.get(0, buff);
            color.get(4 * Float.BYTES, buff);

            vkCmdPushConstants(commandBuffer, graphicsPipeline.pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0, buff);
            model.bind(commandBuffer);
            model.draw(commandBuffer);
        }
    }

    void endSwapChainRenderPass(VkCommandBuffer commandBuffer)
    {
        if (!isFrameStarted)
            throw new RuntimeException("Can't call endFrame while frame is not in progress");

        if (commandBuffer != getCurrentCommandBuffer())
            throw new RuntimeException("Can't end render pass on command buffer from a different frame");

        vkCmdEndRenderPass(commandBuffer);
    }

    public VkCommandBuffer getCurrentCommandBuffer()
    {
        if (!isFrameStarted)
            throw new RuntimeException("Cannot get command buffer when frame not in progress");

        return commands.commandBuffers.get(currentImageIndex);
    }

    public long getSwapChainRenderPass()
    {
        return graphicsPipeline.renderPass;
    }

    boolean isFrameInProgress()
    {
        return isFrameStarted;
    }
}