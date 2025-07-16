package steve6472.flare.assets;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.flare.Commands;
import steve6472.flare.core.Flare;
import steve6472.flare.VkBuffer;
import steve6472.flare.VulkanUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.Channels;
import java.util.function.Consumer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/6/2024
 * Project: Flare <br>
 */
public class Texture
{
    public long textureImage;
    public long textureImageMemory;
    public int width, height;

    // TODO: channels does not work properly
    private void createTextureImage(VkDevice device, long commandPool, VkQueue graphicsQueue, ByteBuffer pixelData, Consumer<ByteBuffer> free, int width, int height, int channels)
    {
        this.width = width;
        this.height = height;

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long imageSize = (long) width * (long) height * (long) channels;
            if (imageSize == 0)
                throw new RuntimeException("Image size is 0!");

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
            }, pixelData);

            free.accept(pixelData);

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            VulkanUtil.createImage(device,
                width, height,
                VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pTextureImage,
                pTextureImageMemory);

            textureImage = pTextureImage.get(0);
            textureImageMemory = pTextureImageMemory.get(0);

            transitionImageLayout(stack, device, textureImage, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, commandPool, graphicsQueue);

            copyBufferToImage(stack, device, stagingBuffer.getBuffer(), textureImage, width, height, commandPool, graphicsQueue);

            transitionImageLayout(stack, device, textureImage, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, commandPool, graphicsQueue);

            stagingBuffer.cleanup();
        }
    }

    public void createTextureImageFromResource(VkDevice device, String path, long commandPool, VkQueue graphicsQueue)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = loadImageAsByteBuffer(path, pWidth, pHeight, pChannels);

            if (pixels == null)
                throw new RuntimeException("Failed to load texture image " + path);

            createTextureImage(device, commandPool, graphicsQueue, pixels, STBImage::stbi_image_free, pWidth.get(0), pHeight.get(0), pChannels.get(0));
        }
    }

    public void createTextureImageFromFile(VkDevice device, String path, long commandPool, VkQueue graphicsQueue)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(path, pWidth, pHeight, pChannels, STBI_rgb_alpha);

            if (pixels == null)
                throw new RuntimeException("Failed to load texture image " + path);

            createTextureImage(device, commandPool, graphicsQueue, pixels, STBImage::stbi_image_free, pWidth.get(0), pHeight.get(0), pChannels.get(0));
        }
    }

    public void createTextureImageFromBufferedImage(VkDevice device, BufferedImage image, long commandPool, VkQueue graphicsQueue)
    {
        ByteBuffer pixels = convertImageToByteBuffer(image);
        createTextureImage(device, commandPool, graphicsQueue, pixels, _ -> {}, image.getWidth(), image.getHeight(), 4);
    }

    public void saveTextureAsPNG(VkDevice device, Commands commandPool, VkQueue graphicsQueue, File outputPath)
    {
        long imageSize = (long) width * (long) height * (long) 4;

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkBuffer stagingBuffer = new VkBuffer(
                device,
                (int) imageSize,
                1,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

            // Transition image layout and copy image to buffer
            VulkanUtil.transitionImageLayout(device, textureImage, VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                commandPool.commandPool, graphicsQueue);

            Texture.copyImageToBuffer(stack, device, textureImage, stagingBuffer.getBuffer(), width, height, commandPool.commandPool, graphicsQueue);

            VulkanUtil.transitionImageLayout(device, textureImage, VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                commandPool.commandPool, graphicsQueue);

            // Map buffer memory
            stagingBuffer.map();
            ByteBuffer data = stagingBuffer.getMappedMemory().getByteBuffer(0, stagingBuffer.getInstanceSize());

            // Write PNG using STB
            STBImageWrite.stbi_write_png(outputPath.getAbsolutePath(), width, height, 4, data, width * 4);

            stagingBuffer.cleanup();
        }
    }

    private static ByteBuffer convertImageToByteBuffer(BufferedImage image)
    {
        // Ensure the image is in a known format like ARGB or RGB
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];

        // Get the ARGB pixel data from the image (using TYPE_INT_ARGB)
        image.getRGB(0, 0, width, height, pixels, 0, width);

        // Create a ByteBuffer to hold the pixel data
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4); // 4 bytes per pixel (ARGB)

        // Copy the pixel data into the ByteBuffer as RGBA
        for (int pixel : pixels)
        {
            // Extract the ARGB components and convert to RGBA (Vulkan prefers this order)
            byte a = (byte) ((pixel >> 24) & 0xFF);  // Alpha
            byte r = (byte) ((pixel >> 16) & 0xFF);  // Red
            byte g = (byte) ((pixel >> 8) & 0xFF);   // Green
            byte b = (byte) (pixel & 0xFF);          // Blue

            buffer.put(r);  // Red
            buffer.put(g);  // Green
            buffer.put(b);  // Blue
            buffer.put(a);  // Alpha
        }

        buffer.flip();  // Prepare the buffer for reading
        return buffer;
    }

    private static ByteBuffer loadImageAsByteBuffer(String resourcePath, IntBuffer width, IntBuffer height, IntBuffer channels)
    {
        ByteBuffer imageBuffer;
        ByteBuffer image;  // 4 means force RGBA
        try
        {
            try (InputStream inputStream = Flare.class.getResourceAsStream(resourcePath))
            {
                if (inputStream == null)
                {
                    throw new IOException("Resource not found: " + resourcePath);
                }

                imageBuffer = BufferUtils.createByteBuffer(inputStream.available());

                Channels.newChannel(inputStream).read(imageBuffer);
                imageBuffer.flip();
            }

            image = stbi_load_from_memory(imageBuffer, width, height, channels, STBI_rgb_alpha);
            if (image == null)
            {
                throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return image;
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

    public static VkBufferImageCopy.Buffer createRegion(MemoryStack stack, int width, int height)
    {
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
        return region;
    }

    public static void copyBufferToImage(MemoryStack stack, VkDevice device, long buffer, long image, int width, int height, long commandPool, VkQueue graphicsQueue)
    {
        VkCommandBuffer commandBuffer = Commands.beginSingleTimeCommands(device, commandPool);

        vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, createRegion(stack, width, height));

        Commands.endSingleTimeCommands(commandBuffer, graphicsQueue, device, commandPool);
    }

    public static void copyImageToBuffer(MemoryStack stack, VkDevice device, long image, long buffer, int width, int height, long commandPool, VkQueue graphicsQueue)
    {
        VkCommandBuffer commandBuffer = Commands.beginSingleTimeCommands(device, commandPool);

        // VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL must be set prior to this call
        vkCmdCopyImageToBuffer(commandBuffer, image, VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, buffer, createRegion(stack, width, height));

        Commands.endSingleTimeCommands(commandBuffer, graphicsQueue, device, commandPool);
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
