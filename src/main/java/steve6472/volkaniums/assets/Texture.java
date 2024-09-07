package steve6472.volkaniums.assets;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.volkaniums.Commands;
import steve6472.volkaniums.VkBuffer;
import steve6472.volkaniums.VulkanUtil;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.registry.Keyable;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/6/2024
 * Project: Volkaniums <br>
 */
public class Texture implements Keyable
{
    public long textureImage;
    public long textureImageMemory;

    public void createTextureImage(VkDevice device, String path, long commandPool, VkQueue graphicsQueue)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(path, pWidth, pHeight, pChannels, STBI_rgb_alpha);

            if (pixels == null)
                throw new RuntimeException("Failed to load texture image " + path);

            long imageSize = (long) pWidth.get(0) * (long) pHeight.get(0) * (long) pChannels.get(0);

            VkBuffer stagingBuffer = new VkBuffer(
                device,
                (int) imageSize,
                1,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

            stagingBuffer.map();
            stagingBuffer.writeToBuffer((byteBuff, offset, data) ->
            {
                for (int i = 0; i < data.capacity(); i++)
                {
                    byteBuff.put(i, data.get(i));
                }
            }, pixels);

            stbi_image_free(pixels);

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            VulkanUtil.createImage(device,
                pWidth.get(0), pHeight.get(0),
                VK_FORMAT_R8G8B8A8_SRGB,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pTextureImage,
                pTextureImageMemory);

            textureImage = pTextureImage.get(0);
            textureImageMemory = pTextureImageMemory.get(0);

            transitionImageLayout(stack, device, textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, commandPool, graphicsQueue);

            copyBufferToImage(stack, device, stagingBuffer.getBuffer(), textureImage, pWidth.get(0), pHeight.get(0), commandPool, graphicsQueue);

            transitionImageLayout(stack, device, textureImage, VK_FORMAT_R8G8B8A8_SRGB, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, commandPool, graphicsQueue);

            stagingBuffer.cleanup();
        }
    }

    private void transitionImageLayout(MemoryStack stack, VkDevice device, long image, int format, int oldLayout, int newLayout, long commandPool, VkQueue graphicsQueue)
    {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.oldLayout(oldLayout);
        barrier.newLayout(newLayout);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.image(image);
        barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange().baseMipLevel(0);
        barrier.subresourceRange().levelCount(1);
        barrier.subresourceRange().baseArrayLayer(0);
        barrier.subresourceRange().layerCount(1);

        int sourceStage;
        int destinationStage;

        if(oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {

            barrier.srcAccessMask(0);
            barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

            sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
            destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;

        } else if(oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {

            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

            sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;

        } else {
            throw new IllegalArgumentException("Unsupported layout transition");
        }

        VkCommandBuffer commandBuffer = Commands.beginSingleTimeCommands(device, commandPool);

        vkCmdPipelineBarrier(commandBuffer,
            sourceStage, destinationStage,
            0,
            null,
            null,
            barrier);

        Commands.endSingleTimeCommands(commandBuffer, graphicsQueue, device, commandPool);
    }

    private void copyBufferToImage(MemoryStack stack, VkDevice device, long buffer, long image, int width, int height, long commandPool, VkQueue graphicsQueue)
    {
        VkCommandBuffer commandBuffer = Commands.beginSingleTimeCommands(device, commandPool);

        VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
        region.bufferOffset(0);
        region.bufferRowLength(0);   // Tightly packed
        region.bufferImageHeight(0);  // Tightly packed
        region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
        region.imageSubresource().mipLevel(0);
        region.imageSubresource().baseArrayLayer(0);
        region.imageSubresource().layerCount(1);
        region.imageOffset().set(0, 0, 0);
        region.imageExtent(VkExtent3D.calloc(stack).set(width, height, 1));

        vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);

        Commands.endSingleTimeCommands(commandBuffer, graphicsQueue, device, commandPool);
    }

    @Override
    public Key key()
    {
        return null;
    }

    public void cleanup(VkDevice device)
    {
        vkDestroyImage(device, textureImage, null);
        vkFreeMemory(device, textureImageMemory, null);
    }

    @Override
    public String toString()
    {
        return "Texture{" + "textureImage=" + textureImage + ", textureImageMemory=" + textureImageMemory + '}';
    }
}
