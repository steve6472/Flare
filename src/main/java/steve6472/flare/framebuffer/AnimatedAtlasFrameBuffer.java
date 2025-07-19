package steve6472.flare.framebuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.flare.Commands;
import steve6472.flare.ErrorCode;
import steve6472.flare.VkBuffer;
import steve6472.flare.VulkanUtil;
import steve6472.flare.assets.Texture;

import java.awt.image.BufferedImage;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/18/2025
 * Project: Flare <br>
 */
public class AnimatedAtlasFrameBuffer
{
    private final VkDevice device;

    public long image;
    public long imageMemory;
    public long imageView;

    public long renderPass;
    public long framebuffer;

    public final int width;
    public final int height;
    public final int imageFormat;
    public final int usage;
    public VkExtent2D extent;

    public AnimatedAtlasFrameBuffer(VkDevice device, int width, int height, int imageFormat, int usage)
    {
        this.device = device;
        this.width = width;
        this.height = height;
        this.imageFormat = imageFormat;
        this.usage = usage;
    }

    public void fromImage(BufferedImage bufferedImage, VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);

            VulkanUtil.createImage(device, width, height, imageFormat, VK_IMAGE_TILING_OPTIMAL, usage, VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, pTextureImage, pTextureImageMemory);

            image = pTextureImage.get(0);
            imageMemory = pTextureImageMemory.get(0);
            imageView = VulkanUtil.createImageView(device, image, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT);


            long imageSize = (long) width * (long) height * (long) 4;
            if (imageSize == 0)
                throw new RuntimeException("Image size is 0!");

            VkBuffer stagingBuffer = new VkBuffer(
                device,
                (int) imageSize,
                1,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

            stagingBuffer.map();
            stagingBuffer.writeToBuffer((byteBuff, _, data) ->
            {
                for (int i = 0; i < data.capacity(); i++)
                {
                    byteBuff.put(i, data.get(i));
                }
            }, Texture.convertImageToByteBuffer(bufferedImage));

            VulkanUtil.transitionImageLayout(device, image, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, commands.commandPool, graphicsQueue);
            Texture.copyBufferToImage(stack, device, stagingBuffer.getBuffer(), image, width, height, commands.commandPool, graphicsQueue);
            VulkanUtil.transitionImageLayout(device, image, imageFormat, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL, commands.commandPool, graphicsQueue);

            stagingBuffer.cleanup();

            extent = VkExtent2D.malloc().set(width, height);
        }
    }

    public void cleanup()
    {
        // Sampler gets cleared from Flare because it is in the registry
        extent.free();
        vkDestroyRenderPass(device, renderPass, null);
        vkDestroyFramebuffer(device, framebuffer, null);
    }

    public void createRenderPass()
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);
            VkAttachmentReference.Buffer refs = VkAttachmentReference.calloc(1, stack);

            // COLOR

            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(imageFormat);
            colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT); // variable
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE); // DONT_CARE disables clearing the image before rendering.. or something
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            colorAttachment.flags(0);

            VkAttachmentReference colorAttachmentRef = refs.get(0);
            colorAttachmentRef.attachment(0);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            // SUBPASS

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.flags(0);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));

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
            LongBuffer attachments = stack.longs(imageView);
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
