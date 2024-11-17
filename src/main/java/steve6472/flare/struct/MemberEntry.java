package steve6472.flare.struct;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public record MemberEntry(MemberData<?> memberData, int size, int arraySize)
{
    private static final int SINGLE_OBJECT = -1;

    public MemberEntry(MemberData<?> memberData, int size)
    {
        this(memberData, size, SINGLE_OBJECT);
    }

    public boolean isArray()
    {
        return arraySize != SINGLE_OBJECT;
    }
}
