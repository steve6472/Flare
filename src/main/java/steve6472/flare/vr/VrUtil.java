package steve6472.flare.vr;

import org.joml.Matrix4f;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;

/**
 * Created by steve6472
 * Date: 9/28/2024
 * Project: Flare <br>
 */
public class VrUtil
{
    public static Matrix4f convertSteamVRMatrixToMatrix4f(HmdMatrix34 matPose)
    {
        return new Matrix4f(
            matPose.m().get(0), matPose.m().get(4), matPose.m().get(8), 0.0f,
            matPose.m().get(1), matPose.m().get(5), matPose.m().get(9), 0.0f,
            matPose.m().get(2), matPose.m().get(6), matPose.m().get(10), 0.0f,
            matPose.m().get(3), matPose.m().get(7), matPose.m().get(11), 1.0f
        );
    }

    public static Matrix4f convertSteamVRMatrixToMatrix4f(HmdMatrix44 matPose)
    {
        return new Matrix4f(
            matPose.m().get(0), matPose.m().get(4), matPose.m().get(8), matPose.m().get(12),
            matPose.m().get(1), matPose.m().get(5), matPose.m().get(9), matPose.m().get(13),
            matPose.m().get(2), matPose.m().get(6), matPose.m().get(10), matPose.m().get(14),
            matPose.m().get(3), matPose.m().get(7), matPose.m().get(11), matPose.m().get(15)
        );
    }

    public static String getRequiredExtensions(long physicalDevice)
    {
        try (MemoryStack stack = stackPush())
        {
            int size = VRCompositor.VRCompositor_GetVulkanDeviceExtensionsRequired(physicalDevice, null);
            ByteBuffer buffer = stack.malloc(size);
            VRCompositor.VRCompositor_GetVulkanDeviceExtensionsRequired(physicalDevice, buffer);
            String extensions = MemoryUtil.memUTF8(buffer);
            extensions = extensions.substring(0, extensions.length() - 1);
            return extensions;
        }
    }
}
