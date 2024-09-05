package steve6472.volkaniums.struct;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public record MemberEntry(MemberData<?> memberData, int alignment, int size, int arraySize)
{
    private static final int SINGLE_OBJECT = -1;

    public MemberEntry(MemberData<?> memberData, int alignment, int size)
    {
        this(memberData, alignment, size, SINGLE_OBJECT);
    }

    public boolean isArray()
    {
        return arraySize != SINGLE_OBJECT;
    }
}
