package steve6472.flare.struct.type;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import steve6472.flare.AlignmentUtils;
import steve6472.flare.struct.MemberData;
import steve6472.flare.struct.StructDef;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public final class StructVertex extends StructDef
{
    public VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack)
    {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);

        bindingDescription.binding(0);
        bindingDescription.stride(sizeof());
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack)
    {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(members.size(), stack);

        int totalOffset = 0;

        for (int i = 0; i < members.size(); i++)
        {
            MemberData<?> member = members.get(i).memberData();

            VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(i);
            posDescription.binding(0);
            posDescription.location(i);
            posDescription.format(member.format());
            posDescription.offset(totalOffset);

            totalOffset += AlignmentUtils.sizeof(member.clazz());
        }

        return attributeDescriptions.rewind();
    }

    public VkPipelineVertexInputStateCreateInfo createVertexInputInfo(MemoryStack stack)
    {
        VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
        vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vertexInputInfo.pVertexBindingDescriptions(getBindingDescription(stack));
        vertexInputInfo.pVertexAttributeDescriptions(getAttributeDescriptions(stack));

        return vertexInputInfo;
    }
}
