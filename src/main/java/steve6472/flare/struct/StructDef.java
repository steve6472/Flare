package steve6472.flare.struct;

import steve6472.core.util.Preconditions;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Flare <br>
 */
public class StructDef
{
    /**
     * Gets calculated in Builder
     * using member size
     */
    int size;
    protected List<MemberEntry> members;
    private MemberData<Struct> memberData;

    public StructDef() {}

    public int sizeof()
    {
        return size;
    }

    public Struct create()
    {
        Struct vertex = new Struct(this.members.size());
        for (int i = 0; i < vertex.members.length; i++)
        {
            vertex.members[i] = members.get(i).memberData().constructor();
        }

        return vertex;
    }

    public Struct create(Object... members)
    {
        Preconditions.checkEqual(this.members.size(), members.length, "Passed members do not have the same count as Vertex members. Expected " + this.members.size() + ", got " + members.length);

        Struct vertex = new Struct(this.members.size());
        for (int i = 0; i < vertex.members.length; i++)
        {
            vertex.members[i] = members[i];
            if (!members[i].getClass().equals(this.members.get(i).memberData().clazz()))
            {
                throw new RuntimeException("Class type mismatch! Passed " + members[i].getClass() + ", expected " + this.members.get(i).memberData().clazz());
            }
        }

        return vertex;
    }

    void createMemberData()
    {
        if (memberData != null)
            throw new RuntimeException("Tried to create member data again! Bad!!!");

        memberData = new MemberData<>(Struct.class, this::create, -1, (buff, offset, obj) ->
        {
            int arrayOffset = 0;
            for (int i = 0; i < members.size(); i++)
            {
                MemberEntry member = members.get(i);
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

    /*
     * Memcpy
     */

    public void memcpy(ByteBuffer buffer, int offset, Collection<Struct> structs)
    {
        int totalOffset = offset;
        for (Struct vertex : structs)
        {
            int localOffset = 0;
            for (int i = 0; i < members.size(); i++)
            {
                MemberEntry memberEntry = members.get(i);
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
        int totalOffset = offset;
        for (Struct vertex : structs)
        {
            int localOffset = 0;
            for (int i = 0; i < members.size(); i++)
            {
                MemberEntry memberEntry = members.get(i);
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
