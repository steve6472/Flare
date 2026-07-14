package steve6472.flare.assets;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.core.log.Log;
import steve6472.flare.Commands;
import steve6472.flare.core.Flare;
import steve6472.flare.VkBuffer;
import steve6472.flare.VulkanUtil;
import steve6472.flare.tracy.FlareProfiler;
import steve6472.flare.tracy.Profiler;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.channels.Channels;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/6/2024
 * Project: Flare <br>
 */
public class Texture
{
    private static final Logger LOGGER = Log.getLogger(Texture.class);
    // Stuff used for vk init
    public int imageUsage = VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT;

    public long textureImage;
    public long textureImageMemory;
    public int width, height;

    // Experimental, turn on in case allocateMemory takes forever ig
    // https://stackoverflow.com/questions/76412671/can-i-reuse-staging-buffer-for-multiple-vertex-buffer-in-vulkan
    public static final int SINGLE_STAGING_BUFFER_SIZE = 8192 * 8192;
    public static boolean SINGLE_STAGING_BUFFER = true;
    public static VkBuffer STAGING;

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

            if (SINGLE_STAGING_BUFFER)
            {
                if (STAGING == null || STAGING.getInstanceSize() < imageSize)
                {
                    if (STAGING != null)
                        STAGING.cleanup();

                    int size = (int) Math.max(SINGLE_STAGING_BUFFER_SIZE, imageSize);
                    LOGGER.info("New size for single staging buffer: " + size);

                    STAGING = new VkBuffer(
                        device,
                        size,
                        1,
                        VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT
                    );
                    STAGING.map();
                }
            } else
            {
                STAGING = new VkBuffer(
                    device,
                    (int) imageSize,
                    1,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);
                STAGING.map();
            }

            STAGING.writeToBuffer((byteBuff, _, data) -> byteBuff.put(data), pixelData);

            free.accept(pixelData);

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);

            VulkanUtil.createImage(device,
                width, height,
                VK_FORMAT_R8G8B8A8_UNORM,
                VK_IMAGE_TILING_OPTIMAL,
                imageUsage,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pTextureImage,
                pTextureImageMemory);

            textureImage = pTextureImage.get(0);
            textureImageMemory = pTextureImageMemory.get(0);

            VulkanUtil.transitionImageLayout(device, textureImage, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, commandPool, graphicsQueue);

            copyBufferToImage(stack, device, STAGING.getBuffer(), textureImage, width, height, commandPool, graphicsQueue);

            VulkanUtil.transitionImageLayout(device, textureImage, VK_FORMAT_R8G8B8A8_UNORM, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, commandPool, graphicsQueue);

            if (!SINGLE_STAGING_BUFFER)
            {
                STAGING.cleanup();
                STAGING = null;
            }
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
        Profiler profiler = FlareProfiler.frame();
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            profiler.push("IO load");
            ByteBuffer pixels = stbi_load(path, pWidth, pHeight, pChannels, STBI_rgb_alpha);

            if (pixels == null)
                throw new RuntimeException("Failed to load texture image " + path + " " + stbi_failure_reason());

            profiler.popPush("createTextureImage");
            createTextureImage(device, commandPool, graphicsQueue, pixels, (_) -> {}, pWidth.get(0), pHeight.get(0), pChannels.get(0));
        }
        profiler.pop();
    }

    public void createTextureImageFromBufferedImage(VkDevice device, BufferedImage image, long commandPool, VkQueue graphicsQueue)
    {
        Profiler profiler = FlareProfiler.frame();
        profiler.push("getPixels");
        ByteBuffer pixels = convertImageToByteBuffer(image);
        profiler.popPush("createTextureImage");
        createTextureImage(device, commandPool, graphicsQueue, pixels, _ -> {}, image.getWidth(), image.getHeight(), 4);
        profiler.pop();
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

    /// Accepts image in format TYPE_4BYTE_ABGR
    public static ByteBuffer convertImageToByteBuffer(BufferedImage image)
    {
        // Ensure the image is in a known format like ARGB or RGB
        int width = image.getWidth();
        int height = image.getHeight();

        // Create a ByteBuffer to hold the pixel data
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4); // 4 bytes per pixel (ARGB)

        // This is optimized as much as I can, I think it's 10x faster than old convertion
        DataBuffer dataBuffer = image.getRaster().getDataBuffer();

        // Copy the pixel data into the ByteBuffer as RGBA
        for (int i = 0, j = 0; i < width * height; i++, j += 4)
        {
            // Extract the components to RGBA (Vulkan prefers this order)
            // The variable names are most likely not correct
            int a = dataBuffer.getElem(0, j);
            int g = dataBuffer.getElem(0, j + 1);
            int b = dataBuffer.getElem(0, j + 2);
            int r = dataBuffer.getElem(0, j + 3);
            buffer.putInt(a | (g << 8) | (b << 16) | (r << 24));
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
                throw new RuntimeException("Failed to load image: " + stbi_failure_reason());
            }
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return image;
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
