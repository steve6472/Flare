package steve6472.flare.struct.type;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.StructDef;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.vkCmdPushConstants;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public final class StructPush extends StructDef
{
    public void push(Struct struct, VkCommandBuffer commandBuffer, long pipelineLayout, int flags, int offset)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer buffer = stack.calloc(sizeof());
            memcpy(buffer, offset, struct);

            vkCmdPushConstants(commandBuffer, pipelineLayout, flags, offset, buffer);
        }
    }
}
