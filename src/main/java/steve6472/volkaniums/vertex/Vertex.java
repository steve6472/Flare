package steve6472.volkaniums.vertex;

import static steve6472.volkaniums.vertex.MemberType.*;

/**
 * Created by steve6472
 * Date: 8/21/2024
 * Project: Volkaniums <br>
 *
 */
public class Vertex
{
    /*
     * Vertex types
     */

    public static final VertexType POS3F_COL3F = builder()
        .addMember(VEC_3F)
        .addMember(VEC_3F)
        .build();

    public static final VertexType POS3F_COL3F_UV = builder()
        .addMember(VEC_3F)
        .addMember(VEC_3F)
        .addMember(UV)
        .build();

    public static final VertexType POS3F_COL3F_NOR3F_UV = builder()
        .addMember(VEC_3F)
        .addMember(VEC_3F)
        .addMember(VEC_3F)
        .addMember(UV)
        .build();

    /*
     * Vertex class def
     */

    Object[] members;

    Vertex() {}

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
}
