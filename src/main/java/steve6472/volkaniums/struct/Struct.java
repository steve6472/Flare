package steve6472.volkaniums.struct;

import java.util.Arrays;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 *
 */
public class Struct
{
    final Object[] members;

    Struct(int memberCount)
    {
        members = new Object[memberCount];
    }

    public <T> T getMember(int index, Class<T> expectedType)
    {
        Object member = members[index];
        if (expectedType.isAssignableFrom(member.getClass()))
            return expectedType.cast(member);
        throw new RuntimeException("Member index: " + index + " " + member.getClass().getSimpleName() + " is not of expected type " + expectedType.getSimpleName());
    }

    public void setMember(int index, Object member)
    {
        if (members[index].getClass() != member.getClass())
        {
            throw new RuntimeException("Setting member to a different class is not possible!");
        }
        members[index] = member;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder("Struct: ");
        for (Object member : members)
        {
            if (member.getClass().isArray())
                s.append("\n\t").append(Arrays.toString((Object[]) member));
            else
                s.append("\n\t").append(member);
        }
        return s.toString();
    }
}
