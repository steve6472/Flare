package steve6472.volkaniums;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.pipeline.Pipelines;
import steve6472.volkaniums.render.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

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

    private final SwapChain swapChain;
    private final Commands commands;
    private final DebugLineRenderSystem debugLineRenderSystem;

    private int currentFrameIndex;
    private SyncFrame thisFrame;
    private int currentImageIndex;

    private boolean isFrameStarted;

    private final List<RenderSystem> renderSystems = new ArrayList<>();

    public MasterRenderer(Window window, VkDevice device, VkQueue graphicsQueue, VkQueue presentQueue, long surface)
    {
        this.window = window;
        this.device = device;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;

        swapChain = new SwapChain(device, window, surface, this);
        commands = new Commands();
        commands.createCommandPool(device, surface);

//        renderSystems.add(new BackdropRenderSystem(device, new Pipeline(Pipelines.BASIC), commands, graphicsQueue));
        renderSystems.add(new BBStaticModelRenderSystem(this, new Pipeline(Pipelines.BB_STATIC)));
//        renderSystems.add(new SBORenderSystem(device, new Pipeline(Pipelines.TEST), commands, graphicsQueue));
        debugLineRenderSystem = new DebugLineRenderSystem(this, new Pipeline(Pipelines.DEBUG_LINE));
        renderSystems.add(debugLineRenderSystem);
//        renderSystems.add(new SkinRenderSystem(this, new Pipeline(Pipelines.SKIN)));

        swapChain.createSwapChainObjects();
    }

    public void rebuildPipelines()
    {
        renderSystems.forEach(renderSystem -> renderSystem.pipeline.rebuild(device, swapChain, renderSystem.setLayouts()));
    }

    public void destroyPipelines()
    {
        renderSystems.forEach(renderSystem -> renderSystem.pipeline.cleanup(device));
    }

    public void cleanup()
    {
        swapChain.cleanupSwapChain();

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
            swapChain.recreateSwapChain();
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
            swapChain.recreateSwapChain();
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

    public Commands getCommands()
    {
        return commands;
    }

    public int getCurrentFrameIndex()
    {
        if (!isFrameStarted)
            throw new RuntimeException("Cannot get frame index when frame not in progress");

        return currentFrameIndex;
    }

    public VkQueue getGraphicsQueue()
    {
        return graphicsQueue;
    }

    public long getSwapChainRenderPass()
    {
        return swapChain.renderPass;
    }

    boolean isFrameInProgress()
    {
        return isFrameStarted;
    }

    public VkDevice getDevice()
    {
        return device;
    }

    public DebugLineRenderSystem debugLines()
    {
        return debugLineRenderSystem;
    }

    public float getAspectRation()
    {
        return swapChain.swapChainExtent.width() / (float) swapChain.swapChainExtent.height();
    }
}