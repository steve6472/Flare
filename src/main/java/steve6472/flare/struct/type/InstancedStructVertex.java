package steve6472.flare.struct.type;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineVertexInputStateCreateInfo;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import steve6472.core.util.Preconditions;
import steve6472.flare.AlignmentUtils;
import steve6472.flare.struct.*;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public final class InstancedStructVertex
{
    public record Binding(int binding, int size, int inputRate, List<MemberEntry> members) {}

    public int totalSize;
    public List<Binding> bindings;
    private MemberData<Struct> memberData;

    public static InstancedBuilder builder()
    {
        return new InstancedBuilder();
    }

    public VkVertexInputBindingDescription.Buffer getBindingDescription(MemoryStack stack)
    {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.calloc(bindings.size(), stack);

        for (Binding binding : bindings)
        {
            bindingDescription.binding(binding.binding());
            bindingDescription.stride(binding.size());
            bindingDescription.inputRate(binding.inputRate());
        }

        return bindingDescription;
    }

    public VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(MemoryStack stack)
    {
        int attributeCount = 0;
        for (Binding binding : bindings)
        {
            attributeCount += binding.members().size();
        }

        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.calloc(attributeCount, stack);

        int totalOffset = 0;
        int totalIndex = 0;
        for (Binding binding : bindings)
        {
            List<MemberEntry> members = binding.members();
            for (MemberEntry memberEntry : members)
            {
                MemberData<?> member = memberEntry.memberData();

                VkVertexInputAttributeDescription posDescription = attributeDescriptions.get(totalIndex);
                posDescription.binding(binding.binding());
                posDescription.location(totalIndex);
                posDescription.format(member.format());
                posDescription.offset(totalOffset);

                totalOffset += AlignmentUtils.sizeof(member.clazz());
                totalIndex++;
            }
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

    public Struct create()
    {
        if (bindings.size() > 1)
            throw new RuntimeException("Idk how to do this");

        List<MemberEntry> theseMembers = bindings.getFirst().members();

        Struct vertex = new Struct(theseMembers.size());
        for (int i = 0; i < vertex.members.length; i++)
        {
            vertex.members[i] = theseMembers.get(i).memberData().constructor().get();
        }

        return vertex;
    }

    public Struct create(Object... members)
    {
        if (bindings.size() > 1)
            throw new RuntimeException("Idk how to do this");

        List<MemberEntry> theseMembers = bindings.getFirst().members();

        Preconditions.checkEqual(theseMembers.size(), members.length, "Passed members do not have the same count as Vertex members. Expected " + theseMembers.size() + ", got " + members.length);

        Struct vertex = new Struct(theseMembers.size());
        for (int i = 0; i < vertex.members.length; i++)
        {
            vertex.members[i] = members[i];
            if (!members[i].getClass().equals(theseMembers.get(i).memberData().clazz()))
            {
                throw new RuntimeException("Class type mismatch! Passed " + members[i].getClass() + ", expected " + theseMembers.get(i).memberData().clazz());
            }
        }

        return vertex;
    }

    public void createMemberData()
    {
        if (memberData != null)
            throw new RuntimeException("Tried to create member data again! Bad!!!");

        List<MemberEntry> theseMembers = bindings.getFirst().members();

        memberData = new MemberData<>(Struct.class, this::create, -1, (buff, offset, obj) ->
        {
            int arrayOffset = 0;
            for (int i = 0; i < theseMembers.size(); i++)
            {
                MemberEntry member = theseMembers.get(i);
                //noinspection unchecked
                ((Memcpy<Object>) member.memberData().memcpy()).accept(buff, offset + arrayOffset, obj.members[i]);
                arrayOffset += member.size();
            }
        });
    }

    public MemberData<Struct> memberData()
    {
        return memberData;
    }

    public int sizeof()
    {
        return totalSize;
    }

    /*
     * Memcpy
     */

    public void memcpy(ByteBuffer buffer, int offset, Collection<Struct> structs)
    {
        if (memberData != null)
            throw new RuntimeException("Tried to create member data again! Bad!!!");

        List<MemberEntry> theseMembers = bindings.getFirst().members();

        int totalOffset = offset;
        for (Struct vertex : structs)
        {
            int localOffset = 0;
            for (int i = 0; i < theseMembers.size(); i++)
            {
                MemberEntry memberEntry = theseMembers.get(i);
                //noinspection unchecked
                MemberData<Object> memberData = (MemberData<Object>) memberEntry.memberData();
                Object member = vertex.members[i];

                memberData.memcpy().accept(buffer, totalOffset + localOffset, member);
                localOffset += memberEntry.size();
            }
            totalOffset += sizeof();
        }
    }

    public void memcpy(ByteBuffer buffer, int offset, Struct... structs)
    {
        if (memberData != null)
            throw new RuntimeException("Tried to create member data again! Bad!!!");

        List<MemberEntry> theseMembers = bindings.getFirst().members();

        int totalOffset = offset;
        for (Struct vertex : structs)
        {
            int localOffset = 0;
            for (int i = 0; i < theseMembers.size(); i++)
            {
                MemberEntry memberEntry = theseMembers.get(i);
                //noinspection unchecked
                MemberData<Object> memberData = (MemberData<Object>) memberEntry.memberData();
                Object member = vertex.members[i];

                memberData.memcpy().accept(buffer, totalOffset + localOffset, member);
                localOffset += memberEntry.size();
            }
            totalOffset += sizeof();
        }
    }
}
