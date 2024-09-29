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
    StructPush SKIN = builder()
        .addMember(INT) // stride
        .build(StructPush::new);

    StructPush STATIC_TRANSFORM_OFFSET = builder()
        .addMember(INT) // offset
        .build(StructPush::new);
}
