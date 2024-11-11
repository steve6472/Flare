package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.render.StaticModelRenderSystem;
import steve6472.volkaniums.struct.StructDef;

import static steve6472.volkaniums.struct.Struct.builder;
import static steve6472.volkaniums.struct.def.MemberType.*;
import static steve6472.volkaniums.struct.def.MemberType.FLOAT;

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

    int MAX_MSDF_FONT_STYLES = 512;

    StructDef MSDF_FONT_STYLE = builder()
        .addMember(VEC_4F) // color
        .addMember(VEC_4F) // outlineColor
        .addMember(VEC_4F) // shadowColor

        .addMember(FLOAT) // softness
        .addMember(FLOAT) // outlineSoftness
        .addMember(FLOAT) // shadowSoftness
        .addMember(FLOAT) // SOFT

        .addMember(FLOAT) // thickness
        .addMember(FLOAT) // outlineThickness
        .addMember(FLOAT) // shadowThickness
        .addMember(FLOAT) // padding

        .addMember(VEC_2F) // shadowOffset
        .addMember(VEC_2F) // atlasSize

        .build();

    StructDef MSDF_FONT_STYLES = builder()
        .addStructArray(MSDF_FONT_STYLE, MAX_MSDF_FONT_STYLES)
        .build();
}
