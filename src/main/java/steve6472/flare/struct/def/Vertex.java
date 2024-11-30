package steve6472.flare.struct.def;

import steve6472.flare.struct.type.StructVertex;

import static steve6472.flare.struct.def.MemberType.*;
import static steve6472.flare.struct.Struct.builder;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public interface Vertex
{
    StructVertex POS3F_COL3F = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_3F)  // color
        .build(StructVertex::new);

    StructVertex SKIN = builder()
        .addMember(VEC_3F)  // position
        .addMember(NORMAL)  // normal
        .addMember(UV)      // uv
        .addMember(INT)     // bone index
        .build(StructVertex::new);

    StructVertex POS3F_COL4F = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_4F)  // color
        .build(StructVertex::new);

    StructVertex POS3F_COL3F_UV = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_3F)  // color
        .addMember(UV)      // uv
        .build(StructVertex::new);

    StructVertex POS3F_COL4F_UV = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_4F)  // color
        .addMember(UV)      // uv
        .build(StructVertex::new);

    StructVertex POS3F_UV = builder()
        .addMember(VEC_3F)  // position
        .addMember(UV)      // uv
        .build(StructVertex::new);

    StructVertex POS3F_UV_INDEX = builder()
        .addMember(VEC_3F)  // position
        .addMember(UV)      // uv
        .addMember(INT)     // font style index
        .build(StructVertex::new);

    StructVertex POS3F_COL3F_DATA3F = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_3F)  // tint
        .addMember(VEC_3F)  // data
        .build(StructVertex::new);

    StructVertex POS3F_NORMAL_UV = builder()
        .addMember(VEC_3F)  // position
        .addMember(NORMAL)  // normal
        .addMember(UV)      // uv
        .build(StructVertex::new);

    StructVertex POS3F_COL3F_NOR3F_UV = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_3F)  // color
        .addMember(NORMAL)  // normal
        .addMember(UV)      // uv
        .build(StructVertex::new);
}
