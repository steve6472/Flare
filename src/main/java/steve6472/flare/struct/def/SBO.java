package steve6472.flare.struct.def;

import steve6472.flare.render.StaticModelRenderSystem;
import steve6472.flare.struct.StructDef;

import static steve6472.flare.struct.Struct.builder;
import static steve6472.flare.struct.def.MemberType.*;
import static steve6472.flare.struct.def.MemberType.FLOAT;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
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
        .addMember(INT)   // soft

        .addMember(FLOAT) // thickness
        .addMember(FLOAT) // outlineThickness
        .addMember(FLOAT) // shadowThickness
        .addMember(INT)   // font index

        .addMember(VEC_2F) // shadowOffset
        .addMember(VEC_2F) // atlasSize

        .build();

    StructDef MSDF_FONT_STYLES = builder()
        .addStructArray(MSDF_FONT_STYLE, MAX_MSDF_FONT_STYLES)
        .build();



    int MAX_SPRITE_ENTRIES = 1024;

    StructDef SPRITE_ENTRY = builder()
        .addMember(VEC_4F) // dimensions
        .addMember(VEC_4F) // border

        //  bits 0-1:
        //      0b00 -> STRETCH
        //      0b01 -> NINE_SLICE
        //      0b10 -> TILED
        //      0b11 -> *unused*
        //
        //  bit 2: T/F -> stretch inner
        //  bit 3:
        //      0b0 -> stretch left
        //      0b1 -> tile left
        //  bit4: T/F -> mirror, used only when border is tiled
        .addMember(INT) // flags
        .addMember(INT) // offset
        .addMember(VEC_2F)

        .build();

    StructDef SPRITE_ENTRIES = builder()
        .addStructArray(SPRITE_ENTRY, MAX_SPRITE_ENTRIES)
        .build();
}
