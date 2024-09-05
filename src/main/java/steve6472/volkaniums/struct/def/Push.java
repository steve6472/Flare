package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.struct.type.StructPush;

import static steve6472.volkaniums.struct.def.MemberType.*;
import static steve6472.volkaniums.struct.Struct.builder;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public interface Push
{
    StructPush PUSH = builder()
        .addMember(MAT_4F) // transformation
        .addMember(VEC_3F) // color
        .build(StructPush::new);
}
