package steve6472.volkaniums.struct;

import steve6472.volkaniums.AlignmentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 */
public final class Builder
{
    private final List<MemberEntry> members = new ArrayList<>();

    public <T> Builder addMember(MemberData<T> memberData)
    {
        members.add(new MemberEntry(memberData, 0, AlignmentUtils.sizeof(memberData.clazz())));
        return this;
    }

    /**
     * @Deprecated Align not finished
     */
    @Deprecated
    public Builder align(int alignment)
    {
        MemberEntry oldEntry = members.removeLast();
        MemberEntry newEntry = new MemberEntry(oldEntry.memberData(), alignment, oldEntry.size() + alignment);
        members.add(newEntry);
        return this;
    }

    public StructDef build()
    {
        StructDef builtVertex = new StructDef();
        builtVertex.members = new ArrayList<>(members.size());
        for (MemberEntry member : members)
        {
            builtVertex.size += member.size();
            builtVertex.members.add(member);
        }

        return builtVertex;
    }

    public <T extends StructDef> T build(Supplier<T> constructor)
    {
        T builtVertex = constructor.get();
        builtVertex.members = new ArrayList<>(members.size());
        for (MemberEntry member : members)
        {
            builtVertex.size += member.size();
            builtVertex.members.add(member);
        }

        return builtVertex;
    }
}
