package steve6472.volkaniums.struct;

import steve6472.volkaniums.AlignmentUtils;
import steve6472.volkaniums.util.Preconditions;

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
        members.add(new MemberEntry(memberData, AlignmentUtils.sizeof(memberData.clazz())));
        return this;
    }

    public <T> Builder addMemberArray(MemberData<T> memberData, int arraySize)
    {
        Preconditions.checkTrue(arraySize < 0, "Array size has to be 0 or more!");

        members.add(new MemberEntry(memberData.makeArray(arraySize), AlignmentUtils.sizeof(memberData.clazz()) * arraySize, arraySize));
        return this;
    }

//    public Builder addStructArray(StructDef struct, int i)
//    {
//        members.add(new MemberEntry(data, 0, struct.sizeof() * i));
//        return this;
//    }

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
