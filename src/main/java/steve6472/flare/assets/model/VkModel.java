package steve6472.flare.assets.model;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.flare.Commands;
import steve6472.flare.VkBuffer;
import steve6472.flare.assets.model.primitive.PrimitiveModel;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.type.StructVertex;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class VkModel
{
    public VkBuffer vertexBuffer;
    public int vertexCount = -1;

    public void destroy()
    {
        vertexBuffer.cleanup();
        vertexBuffer = null;
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

    public void draw(VkCommandBuffer commandBuffer, int instaceCount)
    {
        vkCmdDraw(commandBuffer, vertexCount, instaceCount, 0, 0);
    }

    public void createVertexBuffer(VkDevice device, Commands commands, VkQueue graphicsQueue, PrimitiveModel primitiveModel)
    {
        createVertexBuffer(device, commands, graphicsQueue, primitiveModel.createVerticies(), primitiveModel.vertexType());
    }

    public void createVertexBuffer(VkDevice device, Commands commands, VkQueue graphicsQueue, List<Struct> verticies, StructVertex vertexData)
    {
        if (vertexBuffer != null)
            throw new RuntimeException("Vertex buffer already exists! Can not create model.");

        vertexCount = verticies.size();

        long bufferSize = (long) vertexData.sizeof() * vertexCount;

        VkBuffer stagingBuffer = new VkBuffer(
            device,
            vertexData.sizeof(),
            vertexCount,
            VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT);

        stagingBuffer.map();
        stagingBuffer.writeToBuffer(vertexData::memcpy, verticies);

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
