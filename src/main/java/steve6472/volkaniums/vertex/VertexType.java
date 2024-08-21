package steve6472.volkaniums.vertex;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import steve6472.volkaniums.AlignmentUtils;
import steve6472.volkaniums.util.Preconditions;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public final class VertexType
{
    int size;
    List<MemberData<?>> members;

    VertexType() {}

    public int sizeof()
    {
        return size;
    }

    public Vertex create()
    {
        Vertex vertex = new Vertex();
        vertex.members = new Object[members.size()];
        for (int i = 0; i < vertex.members.length; i++)
        {
            vertex.members[i] = members.get(i).constructor();
        }

        return vertex;
    }

    public Vertex create(Object... members)
    {
        Preconditions.checkEqual(this.members.size(), members.length, "Passed members do not have the same count as Vertex members");

        Vertex vertex = new Vertex();
        vertex.members = new Object[this.members.size()];
        for (int i = 0; i < vertex.members.length; i++)
        {
            vertex.members[i] = members[i];
            if (!members[i].getClass().isAssignableFrom(this.members.get(i).clazz()))
            {
                throw new RuntimeException("Class type mismatch!");
            }
        }

        return vertex;
    }

    /*
     * Vk methods
     */

    public VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack)
    {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(1, stack);

        bindingDescription.binding(0);
        bindingDescription.stride(size);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);

        return bindingDescription;
    }

    public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack)
    {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(members.size(), stack);

        int totalOffset = 0;

        for (int i = 0; i < members.size(); i++)
        {
            MemberData<?> member = members.get(i);

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

    public void memcpy(ByteBuffer buffer, Collection<Vertex> vertices)
    {
        for (Vertex vertex : vertices)
        {
            for (int i = 0; i < members.size(); i++)
            {
                MemberData<Object> memberData = (MemberData<Object>) members.get(i);
                Object member = vertex.members[i];
                memberData.memcpy().accept(buffer, member);
            }
        }
    }

    public void memcpy(ByteBuffer buffer, Vertex[] vertices)
    {
        for (Vertex vertex : vertices)
        {
            for (int i = 0; i < members.size(); i++)
            {
                MemberData<Object> memberData = (MemberData<Object>) members.get(i);
                Object member = vertex.members[i];
                memberData.memcpy().accept(buffer, member);
            }
        }
    }
}
