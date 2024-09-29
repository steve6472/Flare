package steve6472.volkaniums.vr;

import org.joml.Vector4f;
import steve6472.volkaniums.render.debug.DebugRender;

import static org.lwjgl.openvr.VR.*;

/**
 * Created by steve6472
 * Date: 9/28/2024
 * Project: Volkaniums <br>
 */
public enum DeviceType
{
    INVALID             (ETrackedDeviceClass_TrackedDeviceClass_Invalid,            DebugRender.PURPLE),
    HMD                 (ETrackedDeviceClass_TrackedDeviceClass_HMD,                DebugRender.GOLD),
    CONTROLLER          (ETrackedDeviceClass_TrackedDeviceClass_Controller,         DebugRender.RED),
    GENERIC_TRACKER     (ETrackedDeviceClass_TrackedDeviceClass_GenericTracker,     DebugRender.GRAY),
    TRACKING_REFERENCE  (ETrackedDeviceClass_TrackedDeviceClass_TrackingReference,  DebugRender.LIGHT_GRAY),
    MAX                 (ETrackedDeviceClass_TrackedDeviceClass_Max,                DebugRender.DARK_GRAY);

    public final int clazz;
    public final Vector4f debugColor;

    DeviceType(int clazz, Vector4f debugColor)
    {
        this.clazz = clazz;
        this.debugColor = debugColor;
    }

    public static DeviceType getDeviceType(int clazz)
    {
        for (DeviceType value : values())
        {
            if (value.clazz == clazz)
                return value;
        }
        return INVALID;
    }
}
