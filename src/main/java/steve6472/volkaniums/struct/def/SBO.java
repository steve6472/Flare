package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.render.StaticModelRenderSystem;
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
        .addMemberArray(MAT_4F, 32768) // transformations
        .build();

    /// Used by [StaticModelRenderSystem] <br>
    /// Has default size of 32768 transformations
    StructDef BLOCKBENCH_STATIC_TRANSFORMATIONS = builder()
        .addMemberArray(MAT_4F, 32768) // transformations
        .build();
}
