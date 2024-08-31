package steve6472.volkaniums;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.StructDef;
import steve6472.volkaniums.struct.type.StructVertex;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class Model3d
{
    public VkBuffer vertexBuffer;
    public int vertexCount = -1;

    public void destroy()
    {
        vertexBuffer.cleanup();
    }

    public void bind(VkCommandBuffer commandBuffer)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            LongBuffer vertexBuffers = stack.longs(vertexBuffer.getBuffer());
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, vertexBuffers, offsets);
        }
    }

    public void draw(VkCommandBuffer commandBuffer)
    {
        vkCmdDraw(commandBuffer, vertexCount, 1, 0, 0);
    }

    public void createVertexBuffer(VkDevice device, Commands commands, VkQueue graphicsQueue, List<Struct> vertices, StructVertex vertexData)
    {
        vertexCount = vertices.size();

        long bufferSize = (long) vertexData.sizeof() * vertexCount;

        VkBuffer stagingBuffer = new VkBuffer(
            device,
            vertexData.sizeof(),
            vertexCount,
            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        stagingBuffer.map();
        stagingBuffer.writeToBuffer(vertexData::memcpy, vertices);

        vertexBuffer = new VkBuffer(
            device,
            vertexData.sizeof(),
            vertexCount,
            VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
            VK_MEMORY_HEAP_DEVICE_LOCAL_BIT);

        VkBuffer.copyBuffer(commands, device, graphicsQueue, stagingBuffer, vertexBuffer, bufferSize);
        stagingBuffer.cleanup();
    }
}
