package steve6472.volkaniums.vr;

/**
 * Created by steve6472
 * Date: 10/16/2024
 * Project: Volkaniums <br>
 */
public final class VrErrorNames
{
    private VrErrorNames() {}

    public static String EVRInputError(int error)
    {
        return switch (error)
        {
            case 0 -> "EVRInputError_VRInputError_None";
            case 1 -> "EVRInputError_VRInputError_NameNotFound";
            case 2 -> "EVRInputError_VRInputError_WrongType";
            case 3 -> "EVRInputError_VRInputError_InvalidHandle";
            case 4 -> "EVRInputError_VRInputError_InvalidParam";
            case 5 -> "EVRInputError_VRInputError_NoSteam";
            case 6 -> "EVRInputError_VRInputError_MaxCapacityReached";
            case 7 -> "EVRInputError_VRInputError_IPCError";
            case 8 -> "EVRInputError_VRInputError_NoActiveActionSet";
            case 9 -> "EVRInputError_VRInputError_InvalidDevice";
            case 10 -> "EVRInputError_VRInputError_InvalidSkeleton";
            case 11 -> "EVRInputError_VRInputError_InvalidBoneCount";
            case 12 -> "EVRInputError_VRInputError_InvalidCompressedData";
            case 13 -> "EVRInputError_VRInputError_NoData";
            case 14 -> "EVRInputError_VRInputError_BufferTooSmall";
            case 15 -> "EVRInputError_VRInputError_MismatchedActionManifest";
            case 16 -> "EVRInputError_VRInputError_MissingSkeletonData";
            case 17 -> "EVRInputError_VRInputError_InvalidBoneIndex";
            case 18 -> "EVRInputError_VRInputError_InvalidPriority";
            case 19 -> "EVRInputError_VRInputError_PermissionDenied";
            case 20 -> "EVRInputError_VRInputError_InvalidRenderModel";
            default -> throw new IllegalStateException("Unexpected value: " + error);
        };
    }
}
