package steve6472.volkaniums;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.volkaniums.settings.Settings;
import steve6472.volkaniums.vr.VrData;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwWaitEvents;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK13.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class SwapChain
{
    private final VkDevice device;
    private final Window window;
    private final long surface;
    private final MasterRenderer masterRenderer;

    public long swapChain;
    // TODO: LongArrayList
    public List<Long> swapChainImages;
    public List<Long> swapChainImageViews;
    public List<Long> swapChainFramebuffers;
    public int swapChainImageFormat;
    public VkExtent2D swapChainExtent;

    public long depthImageView;
    public long depthImage;
    public long depthImageMemory;
    public long renderPass;

    public int currentFrame;
    public Map<Integer, SyncFrame> imagesInFlight;
    public List<SyncFrame> inFlightFrames;

    public static final int MAX_FRAMES_IN_FLIGHT = 2;

    public SwapChain(VkDevice device, Window window, long surface, MasterRenderer masterRenderer)
    {
        this.device = device;
        this.window = window;
        this.surface = surface;
        this.masterRenderer = masterRenderer;
    }

    public void createSwapChainObjects()
    {
        createSwapChain(surface, device, window.window());
        createImageViews(device);
        createRenderPass(device);
        masterRenderer.rebuildPipelines();
        createDepthResources(device, masterRenderer.getCommands().commandPool, masterRenderer.getGraphicsQueue());
        createFrameBuffers(device);
        masterRenderer.getCommands().createCommandBuffers(device);
        createSyncObjects(device);
    }

    public void recreateSwapChain()
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

    public void cleanupSwapChain()
    {
        swapChainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));

        masterRenderer.getCommands().freeCommandBuffers(device);

        masterRenderer.destroyPipelines();
        vkDestroyRenderPass(device, renderPass, null);
        swapChainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        vkDestroyImageView(device, depthImageView, null);
        vkFreeMemory(device, depthImageMemory, null);
        vkDestroyImage(device, depthImage, null);
        vkDestroySwapchainKHR(device, swapChain, null);
        cleanup(device);
    }

    public void createSwapChain(long surface, VkDevice device, long window)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device.getPhysicalDevice(), stack, surface);

            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
            int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
            VkExtent2D extent = chooseSwapExtent(stack, swapChainSupport.capabilities, window);

            IntBuffer imageCount = stack.ints(swapChainSupport.capabilities.minImageCount() + 1);

            if (swapChainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapChainSupport.capabilities.maxImageCount())
            {
                imageCount.put(0, swapChainSupport.capabilities.maxImageCount());
            }

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(surface);

            // Image settings
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(device.getPhysicalDevice(), surface);

            if (!indices.graphicsFamily.equals(indices.presentFamily))
            {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
            } else
            {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapChainSupport.capabilities.currentTransform());
            createInfo.compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);

            createInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);

            if (vkCreateSwapchainKHR(device, createInfo, null, pSwapChain) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.SWAP_CHAIN_CREATION.format());
            }

            swapChain = pSwapChain.get(0);

            vkGetSwapchainImagesKHR(device, swapChain, imageCount, null);

            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));

            vkGetSwapchainImagesKHR(device, swapChain, imageCount, pSwapchainImages);

            swapChainImages = new ArrayList<>(imageCount.get(0));

            for (int i = 0; i < pSwapchainImages.capacity(); i++)
            {
                swapChainImages.add(pSwapchainImages.get(i));
            }

            swapChainImageFormat = surfaceFormat.format();
            swapChainExtent = VkExtent2D.create().set(extent);
        }
    }

    public void createRenderPass(VkDevice device)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2, stack);
            VkAttachmentReference.Buffer refs = VkAttachmentReference.calloc(2, stack);

            // COLOR

            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(swapChainImageFormat);
            colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference colorAttachmentRef = refs.get(0);
            colorAttachmentRef.attachment(0);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // DEPTH

            VkAttachmentDescription depthAttachment = attachments.get(1);
            depthAttachment.format(findDepthFormat(device, stack));
            depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = refs.get(1);
            depthAttachmentRef.attachment(1);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // SUBPASS

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));
            subpass.pDepthStencilAttachment(depthAttachmentRef);

            VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpass);
            renderPassInfo.pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);

            if (vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.RENDER_PASS_CREATION.format());
            }

            renderPass = pRenderPass.get(0);
        }
    }

    public void createImageViews(VkDevice device)
    {
        swapChainImageViews = new ArrayList<>(swapChainImages.size());

        for (long swapChainImage : swapChainImages)
        {
            swapChainImageViews.add(VulkanUtil.createImageView(device, swapChainImage, swapChainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT));
        }
    }

    public void createFrameBuffers(VkDevice device)
    {
        swapChainFramebuffers = new ArrayList<>(swapChainImageViews.size());

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer attachments = stack.longs(VK_NULL_HANDLE, depthImageView);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            // Lets allocate the create info struct once and just update the pAttachments field each iteration
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(swapChainExtent.width());
            framebufferInfo.height(swapChainExtent.height());
            framebufferInfo.layers(1);

            for (long imageView : swapChainImageViews)
            {
                attachments.put(0, imageView);

                framebufferInfo.pAttachments(attachments);

                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS)
                {
                    throw new RuntimeException(ErrorCode.CREATE_FRAMEBUFFER.format());
                }

                swapChainFramebuffers.add(pFramebuffer.get(0));
            }
        }
    }

    public void createDepthResources(VkDevice device, long commandPool, VkQueue graphicsQueue)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            int depthFormat = findDepthFormat(device, stack);

            LongBuffer pDepthImage = stack.mallocLong(1);
            LongBuffer pDepthImageMemory = stack.mallocLong(1);

            VulkanUtil.createImage(
                device,
                swapChainExtent.width(),
                swapChainExtent.height(),
                depthFormat,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pDepthImage,
                pDepthImageMemory);

            depthImage = pDepthImage.get(0);
            depthImageMemory = pDepthImageMemory.get(0);

            depthImageView = VulkanUtil.createImageView(device, depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT);

            // Explicitly transitioning the depth image
            VulkanUtil.transitionImageLayout(device, depthImage, depthFormat, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, commandPool, graphicsQueue);
        }
    }

    private int findSupportedFormat(VkDevice device, IntBuffer formatCandidates, int tiling, int features)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkFormatProperties props = VkFormatProperties.calloc(stack);

            for (int i = 0; i < formatCandidates.capacity(); ++i)
            {

                int format = formatCandidates.get(i);

                vkGetPhysicalDeviceFormatProperties(device.getPhysicalDevice(), format, props);

                if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features)
                {
                    return format;
                } else if (tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features)
                {
                    return format;
                }

            }
        }

        throw new RuntimeException("Failed to find supported format");
    }

    public int findDepthFormat(VkDevice device, MemoryStack stack)
    {
        return findSupportedFormat(device, stack.ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT), VK_IMAGE_TILING_OPTIMAL, VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats)
    {
        return availableFormats
            .stream()
//            .filter(availableFormat -> availableFormat.format() == (VrData.VR_ON ? VK_FORMAT_B8G8R8A8_UNORM : VK_FORMAT_B8G8R8A8_SRGB))
            .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8A8_UNORM)
            .filter(availableFormat -> availableFormat.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
            .findAny()
            .orElse(availableFormats.get(0));
    }

    private int chooseSwapPresentMode(IntBuffer availablePresentModes)
    {
        for (int i = 0; i < availablePresentModes.capacity(); i++)
        {
            if (availablePresentModes.get(i) == Settings.PRESENT_MODE.get().getVkValue())
            {
                return availablePresentModes.get(i);
            }
        }

        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
    }

    private VkExtent2D chooseSwapExtent(MemoryStack stack, VkSurfaceCapabilitiesKHR capabilities, long window)
    {
        if (capabilities.currentExtent().width() != VulkanUtil.UINT32_MAX)
        {
            return capabilities.currentExtent();
        }

        IntBuffer width = stack.ints(0);
        IntBuffer height = stack.ints(0);

        glfwGetFramebufferSize(window, width, height);

        VkExtent2D actualExtent = VkExtent2D.malloc(stack).set(width.get(0), height.get(0));

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(Math.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(Math.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

        return actualExtent;
    }

    public static SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, MemoryStack stack, long surface)
    {
        SwapChainSupportDetails details = new SwapChainSupportDetails();

        details.capabilities = VkSurfaceCapabilitiesKHR.malloc(stack);
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);

        IntBuffer count = stack.ints(0);

        KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, null);

        if (count.get(0) != 0)
        {
            details.formats = VkSurfaceFormatKHR.malloc(count.get(0), stack);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, count, details.formats);
        }

        KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, null);

        if (count.get(0) != 0)
        {
            details.presentModes = stack.mallocInt(count.get(0));
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, count, details.presentModes);
        }

        return details;
    }

    public void createSyncObjects(VkDevice device)
    {
        inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(swapChainImages.size());

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for (int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++)
            {
                if (vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS ||
                    vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS ||
                    vkCreateFence(device, fenceInfo, null, pFence) != VK_SUCCESS)
                {
                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }

                inFlightFrames.add(new SyncFrame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
            }
        }
    }

    public int acquireNextImage(VkDevice device, IntBuffer pImageIndex, SyncFrame thisFrame)
    {
        vkWaitForFences(device, thisFrame.pFence(), true, VulkanUtil.UINT64_MAX);

        return vkAcquireNextImageKHR(device, swapChain, VulkanUtil.UINT64_MAX, thisFrame.imageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);
    }

    public int submitCommandBuffers(VkDevice device, VkQueue graphicsQueue, VkQueue presentQueue, VkCommandBuffer commandBuffer, IntBuffer pImageIndex, MemoryStack stack, SyncFrame thisFrame)
    {
        VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
        submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);

        submitInfo.waitSemaphoreCount(1);
        submitInfo.pWaitSemaphores(thisFrame.pImageAvailableSemaphore());
        submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));

        submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());

        submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

        vkResetFences(device, thisFrame.pFence());

        if (vkQueueSubmit(graphicsQueue, submitInfo, thisFrame.fence()) != VK_SUCCESS)
        {
            vkResetFences(device, thisFrame.pFence());
            throw new RuntimeException("Failed to submit draw command buffer: "/* + vkResult*/);
        }

        VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
        presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);

        presentInfo.pWaitSemaphores(thisFrame.pRenderFinishedSemaphore());

        presentInfo.swapchainCount(1);
        presentInfo.pSwapchains(stack.longs(swapChain));

        presentInfo.pImageIndices(pImageIndex);

        int vkResult = vkQueuePresentKHR(presentQueue, presentInfo);

        currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;

        return vkResult;
    }

    public void cleanup(VkDevice device)
    {
        inFlightFrames.forEach(frame ->
        {
            vkDestroySemaphore(device, frame.renderFinishedSemaphore(), null);
            vkDestroySemaphore(device, frame.imageAvailableSemaphore(), null);
            vkDestroyFence(device, frame.fence(), null);
        });
        imagesInFlight.clear();
    }
}
