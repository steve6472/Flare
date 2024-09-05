package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.struct.StructDef;

import static steve6472.volkaniums.struct.Struct.builder;
import static steve6472.volkaniums.struct.def.MemberType.MAT_4F;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public interface SBO
{
    StructDef BONES = builder()
        .addMember(MAT_4F) // transformation
        .build();
}
