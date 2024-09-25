package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.struct.StructDef;

import static steve6472.volkaniums.struct.def.MemberType.*;
import static steve6472.volkaniums.struct.Struct.builder;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public interface UBO
{
    StructDef STATIC_BB_MODEL_UBO = builder()
        .addMember(MAT_4F) // projection
        .addMember(MAT_4F) // view
        .build();

    StructDef DEBUG_LINE = builder()
        .addMember(MAT_4F) // projection
        .addMember(MAT_4F) // view
        .build();

    StructDef GLOBAL_UBO_TEST = builder()
        .addMember(MAT_4F) // projection
        .addMember(MAT_4F) // view
        .build();
}
