package steve6472.volkaniums;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;
import steve6472.volkaniums.util.Preconditions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32_SFLOAT;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 *
 */
public class Vertex
{
    /*
     * Member types
     */

    public static final MemberData<Vector3f> VEC_3F = new MemberData<>(
        Vector3f.class,
        Vector3f::new,
        VK_FORMAT_R32G32B32_SFLOAT,
        (buff, obj) -> buff.putFloat(obj.x).putFloat(obj.y).putFloat(obj.z));

    /*
     * Vertex types
     */

    public static final VertexData POS_COL = builder()
        .addMember(VEC_3F)
        .addMember(VEC_3F)
        .build();

    /*
     * Vertex class def
     */

    private int sizeof;
    private Object[] members;

    private Vertex() {}

    public int sizeof()
    {
        return sizeof;
    }

    public <T> T getMember(int index, Class<?> expectedType)
    {
        Object member = members[index];
        if (expectedType.isAssignableFrom(member.getClass()))
            return (T) member;
        throw new RuntimeException("Member index: " + index + " " + member.getClass().getSimpleName() + " is not of expected type " + expectedType.getSimpleName());
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static final class Builder
    {
        private final List<MemberData<?>> members = new ArrayList<>();

        public <T> Builder addMember(Class<T> clazz, Supplier<T> constructor, int format, BiConsumer<ByteBuffer, T> memcpy)
        {
            members.add(new MemberData<>(clazz, constructor, format, memcpy));
            return this;
        }

        public <T> Builder addMember(MemberData<T> memberData)
        {
            members.add(memberData);
            return this;
        }

        public VertexData build()
        {
            VertexData builtVertex = new VertexData();
            builtVertex.members = new ArrayList<>(members.size());
            for (MemberData<?> member : members)
            {
                builtVertex.size += AlignmentUtils.sizeof(member.clazz());
                builtVertex.members.add(member);
            }

            return builtVertex;
        }
    }

    public record MemberData<T>(Class<T> clazz, Supplier<T> constructor, int format, BiConsumer<ByteBuffer, T> memcpy) { }

    public static final class VertexData
    {
        private int size;
        private List<MemberData<?>> members;

        private VertexData() {}

        public int sizeof()
        {
            return size;
        }

        public Vertex create()
        {
            Vertex vertex = new Vertex();
            vertex.sizeof = size;

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
            vertex.sizeof = size;

            vertex.members = new Object[this.members.size()];
            for (int i = 0; i < vertex.members.length; i++)
            {
                vertex.members[i] = members[i];
                if (!members[i].getClass().isAssignableFrom(this.members.get(i).clazz))
                {
                    throw new RuntimeException("Class type mismatch!");
                }
            }

            return vertex;
        }

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
                posDescription.format(member.format);
                posDescription.offset(totalOffset);

                totalOffset += AlignmentUtils.sizeof(member.clazz);
            }

            return attributeDescriptions.rewind();
        }

        public void memcpy(ByteBuffer buffer, Vertex[] vertices)
        {
            for (Vertex vertex : vertices)
            {
                for (int i = 0; i < members.size(); i++)
                {
                    MemberData<Object> memberData = (MemberData<Object>) members.get(i);
                    Object member = vertex.members[i];
                    memberData.memcpy.accept(buffer, member);
                }
            }
        }
    }
}
