package steve6472.volkaniums.struct.def;

import steve6472.volkaniums.settings.VisualSettings;
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
    int GLOBAL_CAMERA_MAX_COUNT = VisualSettings.GLOBAL_CAMERA_COUNT.get();

    StructDef GLOBAL_CAMERA_UBO = builder()
        .addMember(MAT_4F) // projection
        .addMember(MAT_4F) // view
        .setDynamicBufferSize(GLOBAL_CAMERA_MAX_COUNT)
        .build();

    StructDef GLOBAL_UBO_TEST = builder()
        .addMember(MAT_4F) // projection
        .addMember(MAT_4F) // view
        .build();
}
