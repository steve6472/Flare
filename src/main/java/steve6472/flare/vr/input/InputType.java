package steve6472.flare.vr.input;

import org.lwjgl.openvr.*;
import org.lwjgl.system.NativeResource;
import steve6472.core.log.Log;
import steve6472.flare.vr.VrErrorNames;

import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 10/16/2024
 * Project: Flare <br>
 */
public final class InputType<T>
{
    private static final Logger LOGGER = Log.getLogger(InputType.class);

    private final Class<T> clazz;
    private final Supplier<T> constructor;

    public static final InputType<InputAnalogActionData> ANALOG = new InputType<>(InputAnalogActionData.class, InputAnalogActionData::malloc);
    public static final InputType<InputDigitalActionData> DIGITAL = new InputType<>(InputDigitalActionData.class, InputDigitalActionData::malloc);
    public static final InputType<InputSkeletalActionData> SKELETAL = new InputType<>(InputSkeletalActionData.class, InputSkeletalActionData::malloc);

    private static final Supplier<InputPoseActionData> POSE_CONSTRUCTOR = InputPoseActionData::malloc;

    public static final InputType<InputPoseActionData> POSE_SEATED_NEXT = new InputType<>(InputPoseActionData.class, POSE_CONSTRUCTOR);
    public static final InputType<InputPoseActionData> POSE_STANDING_NEXT = new InputType<>(InputPoseActionData.class, POSE_CONSTRUCTOR);
    public static final InputType<InputPoseActionData> POSE_RAW_NEXT = new InputType<>(InputPoseActionData.class, POSE_CONSTRUCTOR);

    public static final InputType<InputPoseActionData> POSE_SEATED_RELATIVE = new InputType<>(InputPoseActionData.class, POSE_CONSTRUCTOR);
    public static final InputType<InputPoseActionData> POSE_STANDING_RELATIVE = new InputType<>(InputPoseActionData.class, POSE_CONSTRUCTOR);
    public static final InputType<InputPoseActionData> POSE_RAW_RELATIVE = new InputType<>(InputPoseActionData.class, POSE_CONSTRUCTOR);

    private InputType(Class<T> clazz, Supplier<T> constructor)
    {
        this.clazz = clazz;
        this.constructor = constructor;
    }

    public Class<T> getTypeClass()
    {
        return clazz;
    }

    public T construct()
    {
        return constructor.get();
    }

    void cleanup(VrAction<T> action)
    {
        if (action.actionData instanceof NativeResource nr)
            nr.free();
        else
            LOGGER.severe("Could not cleanup input type of " + getTypeClass().getSimpleName() + ", not NativeResource");
    }

    void update(VrAction<T> action, float prediction)
    {
        int err;

        if (this == ANALOG)
        {
            if ((err = VRInput.VRInput_GetAnalogActionData(action.handle(), (InputAnalogActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));

        } else if (this == DIGITAL)
        {
            if ((err = VRInput.VRInput_GetDigitalActionData(action.handle(), (InputDigitalActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == POSE_SEATED_NEXT)
        {
            if ((err = VRInput.VRInput_GetPoseActionDataForNextFrame(action.handle(), VR.ETrackingUniverseOrigin_TrackingUniverseSeated, (InputPoseActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == POSE_STANDING_NEXT)
        {
            if ((err = VRInput.VRInput_GetPoseActionDataForNextFrame(action.handle(), VR.ETrackingUniverseOrigin_TrackingUniverseStanding, (InputPoseActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == POSE_RAW_NEXT)
        {
            if ((err = VRInput.VRInput_GetPoseActionDataForNextFrame(action.handle(), VR.ETrackingUniverseOrigin_TrackingUniverseRawAndUncalibrated, (InputPoseActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == POSE_SEATED_RELATIVE)
        {
            if ((err = VRInput.VRInput_GetPoseActionDataRelativeToNow(action.handle(), VR.ETrackingUniverseOrigin_TrackingUniverseSeated, prediction, (InputPoseActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == POSE_STANDING_RELATIVE)
        {
            if ((err = VRInput.VRInput_GetPoseActionDataRelativeToNow(action.handle(), VR.ETrackingUniverseOrigin_TrackingUniverseStanding, prediction, (InputPoseActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == POSE_RAW_RELATIVE)
        {
            if ((err = VRInput.VRInput_GetPoseActionDataRelativeToNow(action.handle(), VR.ETrackingUniverseOrigin_TrackingUniverseRawAndUncalibrated, prediction, (InputPoseActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else if (this == SKELETAL)
        {
            if ((err = VRInput.VRInput_GetSkeletalActionData(action.handle(), (InputSkeletalActionData) action.actionData, 0)) != VR.EVRInputError_VRInputError_None)
                LOGGER.severe("Updating analog action data error: " + VrErrorNames.EVRInputError(err));
        } else
        {
            LOGGER.severe("Unknown action type? " + this);
        }
    }
}
