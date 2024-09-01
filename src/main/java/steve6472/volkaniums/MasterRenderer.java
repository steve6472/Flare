package steve6472.volkaniums;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.pipeline.Pipelines;
import steve6472.volkaniums.render.ModelRenderSystem;
import steve6472.volkaniums.render.RenderSystem;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public class MasterRenderer
{
    private final Window window;
    private final VkDevice device;
    private final VkQueue graphicsQueue;
    private final VkQueue presentQueue;
    private final long surface;

    private final SwapChain swapChain;
    private final Pipeline graphicsPipeline;
    private final Commands commands;
    private final long globalSetLayout;

    private int currentFrameIndex;
    private SyncFrame thisFrame;
    private int currentImageIndex;

    private boolean isFrameStarted;

    private List<RenderSystem> renderSystems = new ArrayList<>();

    public MasterRenderer(Window window, VkDevice device, VkQueue graphicsQueue, VkQueue presentQueue, long surface, long globalSetLayout)
    {
        this.window = window;
        this.device = device;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
        this.surface = surface;

        swapChain = new SwapChain();
        graphicsPipeline = new Pipeline(Pipelines.BASIC);
        commands = new Commands();
        this.globalSetLayout = globalSetLayout;
        commands.createCommandPool(device, surface);

        renderSystems.add(new ModelRenderSystem(device, graphicsPipeline, commands, graphicsQueue));

        createSwapChainObjects();
    }

    private void createSwapChainObjects()
    {
        swapChain.createSwapChain(surface, device, window.window());
        swapChain.createImageViews(device);
        swapChain.createRenderPass(device, swapChain);
        graphicsPipeline.rebuild(device, swapChain, globalSetLayout);
        swapChain.createDepthResources(device, commands.commandPool, graphicsQueue);
        swapChain.createFrameBuffers(device);
        commands.createCommandBuffers(device);
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

        vkDestroyPipeline(device, graphicsPipeline.graphicsPipeline(), null);
        vkDestroyPipelineLayout(device, graphicsPipeline.pipelineLayout(), null);
        vkDestroyRenderPass(device, swapChain.renderPass, null);
        swapChain.swapChainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        vkDestroyImageView(device, swapChain.depthImageView, null);
        vkFreeMemory(device, swapChain.depthImageMemory, null);
        vkDestroyImage(device, swapChain.depthImage, null);
        vkDestroySwapchainKHR(device, swapChain.swapChain, null);
        swapChain.cleanup(device);
    }

    public void cleanup()
    {
        cleanupSwapChain();

        renderSystems.forEach(RenderSystem::cleanup);

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
        currentFrameIndex = (currentFrameIndex + 1) % SwapChain.MAX_FRAMES_IN_FLIGHT;
    }

    void beginSwapChainRenderPass(VkCommandBuffer commandBuffer, MemoryStack stack)
    {
        if (!isFrameStarted)
            throw new RuntimeException("Can't call beginSwapChainRenderPass while already in progress");

        if (commandBuffer != getCurrentCommandBuffer())
            throw new RuntimeException("Can't begin render pass on command buffer from a different frame");

        VkRenderPassBeginInfo renderPassInfo = Commands.createRenderPass(stack, swapChain);

        renderPassInfo.framebuffer(swapChain.swapChainFramebuffers.get(currentImageIndex));

        vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
    }

    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        for (RenderSystem renderSystem : renderSystems)
        {
            renderSystem.render(frameInfo, stack);
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

        return commands.commandBuffers.get(currentFrameIndex);
    }

    public int getCurrentFrameIndex()
    {
        if (!isFrameStarted)
            throw new RuntimeException("Cannot get frame index when frame not in progress");

        return currentFrameIndex;
    }

    public long getSwapChainRenderPass()
    {
        return swapChain.renderPass;
    }

    boolean isFrameInProgress()
    {
        return isFrameStarted;
    }

    public float getAspectRation()
    {
        return swapChain.swapChainExtent.width() / (float) swapChain.swapChainExtent.height();
    }
}