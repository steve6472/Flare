package steve6472.flare.framebuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.flare.ErrorCode;
import steve6472.flare.VulkanUtil;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/26/2024
 * Project: Flare <br>
 */
public class VRFrameBuffer
{
    private final VkDevice device;

    public long image;
    public long imageMemory;
    public long imageView;
    public long depthImage;
    public long depthImageMemory;
    public long depthImageView;
    public long renderPass;
    public long framebuffer;

    public int imageLayout;
    public int depthImageLayout;

    public final int width;
    public final int height;
    public final int imageFormat;
    public final int usage;

    public VRFrameBuffer(VkDevice device, int width, int height, int imageFormat, int usage)
    {
        this.device = device;
        this.width = width;
        this.height = height;
        this.imageFormat = imageFormat;
        this.usage = usage;

        createImage();
        createView();
        createDepthStencilTarget();
        createDepthStencilView();
        createRenderPass();
        createFrameBuffer();
        imageLayout = VK_IMAGE_LAYOUT_UNDEFINED;
        depthImageLayout = VK_IMAGE_LAYOUT_UNDEFINED;
    }

    public void cleanup()
    {
        vkDestroyImageView(device, imageView, null);
        vkDestroyImage(device, image, null);
        vkFreeMemory(device, imageMemory, null);
        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);
        vkDestroyRenderPass(device, renderPass, null);
        vkDestroyFramebuffer(device, framebuffer, null);
    }

    public long image() { return image; }
    public long imageView() { return imageView; }
    public long imageMemory() { return imageMemory; }
    public long depthImage() { return depthImage; }
    public long depthImageView() { return depthImageView; }
    public long depthImageMemory() { return depthImageMemory; }
    public long renderPass() { return renderPass; }
    public long framebuffer() { return framebuffer; }
    public int imageLayout() { return imageLayout; }
    public int depthImageLayout() { return depthImageLayout; }

    public void image(long image) { this.image = image; }
    public void imageView(long imageView) { this.imageView = imageView; }
    public void imageMemory(long imageMemory) { this.imageMemory = imageMemory; }
    public void depthImage(long depthImage) { this.depthImage = depthImage; }
    public void depthImageView(long depthImageView) { this.depthImageView = depthImageView; }
    public void depthImageMemory(long depthImageMemory) { this.depthImageMemory = depthImageMemory; }
    public void renderPass(long renderPass) { this.renderPass = renderPass; }
    public void framebuffer(long framebuffer) { this.framebuffer = framebuffer; }
    public void imageLayout(int imageLayout) { this.imageLayout = imageLayout; }
    public void depthImageLayout(int depthImageLayout) { this.depthImageLayout = depthImageLayout; }

    public void createImage()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);

            VulkanUtil.createImage(device, width, height, imageFormat, VK_IMAGE_TILING_OPTIMAL, usage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pTextureImage, pTextureImageMemory);

            image = pTextureImage.get(0);
            imageMemory = pTextureImageMemory.get(0);
        }
    }

    public void createView()
    {
        imageView = VulkanUtil.createImageView(device, image, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT);
    }

    public void createSampler(int filter, int mipmapMode, boolean anisotropy)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(filter);
            samplerInfo.minFilter(filter);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(anisotropy);
            samplerInfo.maxAnisotropy(16f);
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(mipmapMode);

            LongBuffer pTextureSampler = stack.mallocLong(1);

            if (vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS)
            {
                throw new RuntimeException("Fialed to create texture sampler");
            }
        }
    }

    public void createDepthStencilTarget()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);

            VulkanUtil.createImage(device, width, height, VK_FORMAT_D32_SFLOAT, VK_IMAGE_TILING_OPTIMAL, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pTextureImage, pTextureImageMemory);

            depthImage = pTextureImage.get(0);
            depthImageMemory = pTextureImageMemory.get(0);
        }
    }

    public void createDepthStencilView()
    {
        depthImageView = VulkanUtil.createImageView(device, depthImage, VK_FORMAT_D32_SFLOAT, VK_IMAGE_ASPECT_DEPTH_BIT);
    }

    public void createRenderPass()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2, stack);
            VkAttachmentReference.Buffer refs = VkAttachmentReference.calloc(2, stack);

            // COLOR

            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(imageFormat);
            colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT); // variable
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            colorAttachment.flags(0);

            VkAttachmentReference colorAttachmentRef = refs.get(0);
            colorAttachmentRef.attachment(0);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // DEPTH

            VkAttachmentDescription depthAttachment = attachments.get(1);
            depthAttachment.format(VK_FORMAT_D32_SFLOAT); // should use the findFormat func from swapchain
            depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT); // variable
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = refs.get(1);
            depthAttachmentRef.attachment(1);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            // SUBPASS

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.flags(0);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));
            subpass.pDepthStencilAttachment(depthAttachmentRef);

            VkSubpassDependency.Buffer dependencies = VkSubpassDependency.calloc(2, stack);

            // First dependency (External -> Subpass)
            VkSubpassDependency dependency1 = dependencies.get(0);
            dependency1.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency1.dstSubpass(0);
            dependency1.srcStageMask(VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT | VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency1.srcAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            dependency1.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT);
            dependency1.dstAccessMask(VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT | VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            // Second dependency (Subpass -> External)
            VkSubpassDependency dependency2 = dependencies.get(1);
            dependency2.srcSubpass(0);
            dependency2.dstSubpass(VK_SUBPASS_EXTERNAL);
            dependency2.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK_PIPELINE_STAGE_LATE_FRAGMENT_TESTS_BIT);
            dependency2.srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            dependency2.dstStageMask(VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT | VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT);
            dependency2.dstAccessMask(VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_READ_BIT);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.flags(0);
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpass);
            renderPassInfo.pDependencies(dependencies);

            LongBuffer pRenderPass = stack.mallocLong(1);

            if (vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.RENDER_PASS_CREATION.format());
            }

            renderPass = pRenderPass.get(0);
        }
    }

    public void createFrameBuffer()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer attachments = stack.longs(imageView, depthImageView);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferCreateInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferCreateInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferCreateInfo.renderPass(renderPass);
            framebufferCreateInfo.pAttachments(attachments);
            framebufferCreateInfo.width(width);
            framebufferCreateInfo.height(height);
            framebufferCreateInfo.layers(1);

            if (vkCreateFramebuffer(device, framebufferCreateInfo, null, pFramebuffer) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.CREATE_FRAMEBUFFER.format());
            }

            framebuffer = pFramebuffer.get(0);
        }
    }
}
