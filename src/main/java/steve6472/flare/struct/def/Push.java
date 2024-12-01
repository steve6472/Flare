package steve6472.flare.struct.def;

import steve6472.flare.struct.type.StructPush;

import static steve6472.flare.struct.def.MemberType.*;
import static steve6472.flare.struct.Struct.builder;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
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
