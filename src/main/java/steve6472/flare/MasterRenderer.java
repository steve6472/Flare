package steve6472.flare;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.framebuffer.AnimatedAtlasFrameBuffer;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.render.*;
import steve6472.flare.struct.def.UBO;
import steve6472.flare.ui.font.render.TextRender;
import steve6472.flare.vr.VrData;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Flare <br>
 */
public class MasterRenderer
{
    private final Window window;
    private final VkDevice device;
    private final VkQueue graphicsQueue;
    private final VkQueue presentQueue;

    private final SwapChain swapChain;
    private final Commands commands;
    private final VrData vrData;
    private final TextRender textRender;

    private int currentFrameIndex;
    private SyncFrame thisFrame;
    private int currentImageIndex;

    private boolean isFrameStarted;

    private final List<AnimateTextureSystem> atlasAnimations = new ArrayList<>();
    private final List<RenderSystem> renderSystems = new ArrayList<>();

    public MasterRenderer(Window window, VkDevice device, VkQueue graphicsQueue, VkQueue presentQueue, Commands commands, long surface, VrData vrData)
    {
        this.window = window;
        this.device = device;
        this.graphicsQueue = graphicsQueue;
        this.presentQueue = presentQueue;
        this.commands = commands;
        this.vrData = vrData;
        this.textRender = new TextRender();

        swapChain = new SwapChain(device, window, surface, this);
    }

    public void builtinLast()
    {
        addRenderSystem(new DebugLineRenderSystem(this, Pipelines.DEBUG_LINE));
        addRenderSystem(new FontRenderSystem(this, Pipelines.FONT_SDF));
    }

    public void addRenderSystem(RenderSystem renderSystem)
    {
        renderSystems.add(renderSystem);
    }

    public void addAtlasAnimationSystem(AnimateTextureSystem renderSystem)
    {
        atlasAnimations.add(renderSystem);
    }

    public void rebuildPipelines()
    {
        renderSystems.forEach(renderSystem -> renderSystem._getPipeline().rebuild(device, swapChain, renderSystem.setLayouts()));
        atlasAnimations.forEach(renderSystem -> renderSystem._getPipeline().rebuild(device, renderSystem.atlas.frameBuffer.extent, renderSystem.atlas.frameBuffer.renderPass, renderSystem.setLayouts()));

        if (VrData.VR_ON)
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                VkExtent2D extent = VkExtent2D.calloc(stack).set(vrData.width(), vrData.height());
                long renderPass = vrData.renderPass();
                renderSystems.forEach(renderSystem -> renderSystem._getVrPipeline().rebuild(device, extent, renderPass, renderSystem.setLayouts()));
            }
        }
    }

    public void destroyPipelines()
    {
        renderSystems.forEach(renderSystem -> renderSystem._getPipeline().cleanup(device));
        atlasAnimations.forEach(renderSystem -> renderSystem._getPipeline().cleanup(device));
        if (VrData.VR_ON)
            renderSystems.forEach(renderSystem -> renderSystem._getVrPipeline().cleanup(device));
    }

    public void cleanup()
    {
        swapChain.cleanupSwapChain();

        renderSystems.forEach(RenderSystem::cleanup);
        atlasAnimations.forEach(AnimateTextureSystem::cleanup);

        vkDestroyCommandPool(device, commands.commandPool, null);
        window.cleanup();
    }

    public VkCommandBuffer beginFrame(MemoryStack stack, IntBuffer pImageIndex)
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

    public void endFrame(MemoryStack stack, IntBuffer pImageIndex)
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

    public void beginSwapChainRenderPass(VkCommandBuffer commandBuffer, MemoryStack stack)
    {
        beginRenderPass(commandBuffer, stack, swapChain.renderPass, swapChain.swapChainExtent, swapChain.swapChainFramebuffers.getLong(currentImageIndex));
    }

    public void beginRenderPass(VkCommandBuffer commandBuffer, MemoryStack stack, long renderPass, VkExtent2D extent, long frameBuffer)
    {
        if (!isFrameStarted)
            throw new RuntimeException("Can't call beginRenderPass while already in progress");

        if (commandBuffer != getCurrentCommandBuffer())
            throw new RuntimeException("Can't begin render pass on command buffer from a different frame");

        VkRenderPassBeginInfo renderPassInfo = Commands.createRenderPass(stack, renderPass, extent);

        renderPassInfo.framebuffer(frameBuffer);

        vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
    }

//    public int totalRenderCount = 0;
//    public int maxRenderCount = 0;

    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        if (frameInfo.camera().cameraIndex >= UBO.GLOBAL_CAMERA_MAX_COUNT)
            throw new RuntimeException("Too many scene renders within one frame!");

        for (RenderSystem renderSystem : renderSystems)
        {
            renderSystem.render(frameInfo, stack);
        }

//        if (VisualSettings.DEBUG_LINE_SINGLE_BUFFER.get())
//        {
//            totalRenderCount++;
//            if (totalRenderCount == maxRenderCount)
//            {
//                DebugRender.getInstance().clearOldVerticies();
//            }
//        }

        frameInfo.camera().cameraIndex++;
    }

    public void updateAtlasAnimations(FrameInfo frameInfo, MemoryStack stack)
    {
        for (AnimateTextureSystem atlasAnimation : atlasAnimations)
        {
            AnimatedAtlasFrameBuffer frameBuffer = atlasAnimation.atlas.frameBuffer;
            beginRenderPass(frameInfo.commandBuffer(), stack, frameBuffer.renderPass, frameBuffer.extent, frameBuffer.framebuffer);
            atlasAnimation.render(frameInfo, stack);
            endRenderPass(frameInfo.commandBuffer());
        }
    }

    public void postFrame()
    {
        for (RenderSystem renderSystem : renderSystems)
        {
            renderSystem.postFrame();
        }
    }

    public void endRenderPass(VkCommandBuffer commandBuffer)
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

    public SwapChain getSwapChain()
    {
        return swapChain;
    }

    public boolean isFrameInProgress()
    {
        return isFrameStarted;
    }

    public VkDevice getDevice()
    {
        return device;
    }

    public VrData getVrData()
    {
        return vrData;
    }

    public Window getWindow()
    {
        return window;
    }

    public TextRender textRender()
    {
        return textRender;
    }

    public float getAspectRatio()
    {
        return swapChain.swapChainExtent.width() / (float) swapChain.swapChainExtent.height();
    }
}