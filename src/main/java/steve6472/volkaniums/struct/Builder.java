package steve6472.volkaniums.struct;

import steve6472.core.util.Preconditions;
import steve6472.volkaniums.AlignmentUtils;

import java.lang.reflect.Member;
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
    private int dynamicBufferSize = 1;

    public <T> Builder addMember(MemberData<T> memberData)
    {
        members.add(new MemberEntry(memberData, AlignmentUtils.sizeof(memberData.clazz())));
        return this;
    }

    public <T> Builder addMemberArray(MemberData<T> memberData, int arraySize)
    {
        Preconditions.checkTrue(arraySize < 0, "Array size has to be 0 or more!");

        members.add(new MemberEntry(memberData.makeArray(arraySize, AlignmentUtils.sizeof(memberData.clazz())), AlignmentUtils.sizeof(memberData.clazz()) * arraySize, arraySize));
        return this;
    }

    public Builder addStruct(StructDef struct)
    {
        members.add(new MemberEntry(struct.memberData(), struct.sizeof()));
        return this;
    }

    public Builder addStructArray(StructDef struct, int arraySize)
    {
        members.add(new MemberEntry(struct.memberData().makeArray(arraySize, struct.sizeof()), struct.sizeof() * arraySize, arraySize));
        return this;
    }

    /// Multiplies the size calculated from the members by [#dynamicBufferSize]
    public Builder setDynamicBufferSize(int dynamicBufferSize)
    {
        this.dynamicBufferSize = dynamicBufferSize;
        return this;
    }

    public StructDef build()
    {
        StructDef builtStruct = new StructDef();
        builtStruct.members = new ArrayList<>(members.size());
        for (MemberEntry member : members)
        {
            builtStruct.size += member.size();
            builtStruct.members.add(member);
        }
        builtStruct.size *= dynamicBufferSize;
        builtStruct.createMemberData();

        return builtStruct;
    }

    public <T extends StructDef> T build(Supplier<T> constructor)
    {
        T builtStruct = constructor.get();
        builtStruct.members = new ArrayList<>(members.size());
        for (MemberEntry member : members)
        {
            builtStruct.size += member.size();
            builtStruct.members.add(member);
        }
        builtStruct.size *= dynamicBufferSize;
        builtStruct.createMemberData();

        return builtStruct;
    }
}
