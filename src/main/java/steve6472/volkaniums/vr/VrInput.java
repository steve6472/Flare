package steve6472.volkaniums.vr;

import com.mojang.datafixers.util.Pair;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Created by steve6472
 * Date: 10/2/2024
 * Project: Volkaniums <br>
 */
public class VrInput
{
    private final VrData vrData;

    VrInput(VrData vrData)
    {
        this.vrData = vrData;
    }

    public List<Pair<DeviceType, Matrix4f>> getPoses()
    {
        return vrData.getPoses();
    }
}
