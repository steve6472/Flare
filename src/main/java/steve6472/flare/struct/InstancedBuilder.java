package steve6472.flare.struct;

import steve6472.core.util.Preconditions;
import steve6472.flare.AlignmentUtils;
import steve6472.flare.struct.type.InstancedStructVertex;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Flare <br>
 */
public final class InstancedBuilder
{
    private final List<List<MemberEntry>> members = new ArrayList<>(1);
    private final List<Integer> inputRates = new ArrayList<>(1);
    private int currentBinding = 0;

    public InstancedBuilder()
    {
        members.add(new ArrayList<>());
        inputRates.add(VK_VERTEX_INPUT_RATE_VERTEX);
    }

    public <T> InstancedBuilder addMember(MemberData<T> memberData)
    {
        members.get(currentBinding).add(new MemberEntry(memberData, AlignmentUtils.sizeof(memberData.clazz())));
        return this;
    }

    public <T> InstancedBuilder addMemberArray(MemberData<T> memberData, int arraySize)
    {
        Preconditions.checkTrue(arraySize < 0, "Array size has to be 0 or more!");

        members.get(currentBinding).add(new MemberEntry(memberData.makeArray(arraySize, AlignmentUtils.sizeof(memberData.clazz())), AlignmentUtils.sizeof(memberData.clazz()) * arraySize, arraySize));
        return this;
    }

    public InstancedBuilder addStruct(StructDef struct)
    {
        members.get(currentBinding).add(new MemberEntry(struct.memberData(), struct.sizeof()));
        return this;
    }

    public InstancedBuilder addStructArray(StructDef struct, int arraySize)
    {
        members.get(currentBinding).add(new MemberEntry(struct.memberData().makeArray(arraySize, struct.sizeof()), struct.sizeof() * arraySize, arraySize));
        return this;
    }

    public InstancedBuilder inputRate(int inputRate)
    {
        this.inputRates.set(currentBinding, inputRate);
        return this;
    }

    public InstancedBuilder nextBinding()
    {
        currentBinding++;
        members.add(new ArrayList<>());
        inputRates.add(VK_VERTEX_INPUT_RATE_VERTEX);
        return this;
    }

    public InstancedStructVertex build()
    {
        InstancedStructVertex builtStruct = new InstancedStructVertex();
        builtStruct.bindings = new ArrayList<>(members.size());
        for (int bindingIndex = 0; bindingIndex < members.size(); bindingIndex++)
        {
            List<MemberEntry> memberBinding = members.get(bindingIndex);

            int size = 0;
            List<MemberEntry> memberEntries = new ArrayList<>(memberBinding.size());

            for (MemberEntry memberEntry : memberBinding)
            {
                size += memberEntry.size();
                memberEntries.add(memberEntry);
            }

            builtStruct.bindings.add(new InstancedStructVertex.Binding(bindingIndex, size, inputRates.get(bindingIndex), memberEntries));
            builtStruct.totalSize += size;
        }
        builtStruct.createMemberData();

        return builtStruct;
    }
}
