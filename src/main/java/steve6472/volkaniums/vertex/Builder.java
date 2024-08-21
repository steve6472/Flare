package steve6472.volkaniums.vertex;

import steve6472.volkaniums.AlignmentUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public final class Builder
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

    public VertexType build()
    {
        VertexType builtVertex = new VertexType();
        builtVertex.members = new ArrayList<>(members.size());
        for (MemberData<?> member : members)
        {
            builtVertex.size += AlignmentUtils.sizeof(member.clazz());
            builtVertex.members.add(member);
        }

        return builtVertex;
    }
}
