package steve6472.volkaniums.struct.type;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import steve6472.volkaniums.struct.Struct;
import steve6472.volkaniums.struct.StructDef;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public final class StructPush extends StructDef
{
    public void push(Struct struct, VkCommandBuffer commandBuffer, long pipelineLayout, int flags, int offset)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.calloc(sizeof());
            memcpy(buffer, struct);

            vkCmdPushConstants(commandBuffer, pipelineLayout, flags, offset, buffer);
        }
    }
}
