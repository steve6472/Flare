package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.struct.type.StructVertex;

import static steve6472.volkaniums.struct.def.MemberType.*;
import static steve6472.volkaniums.struct.Struct.builder;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public interface Vertex
{
    StructVertex POS3F_COL3F = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_3F)  // color
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

    StructVertex POS3F_COL3F_NOR3F_UV = builder()
        .addMember(VEC_3F)  // position
        .addMember(VEC_3F)  // color
        .addMember(VEC_3F)  // normal
        .addMember(UV)      // uv
        .build(StructVertex::new);
}
