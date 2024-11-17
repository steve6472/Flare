package steve6472.flare;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

import java.nio.IntBuffer;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class SwapChainSupportDetails
{
    public VkSurfaceCapabilitiesKHR capabilities;
    public VkSurfaceFormatKHR.Buffer formats;
    public IntBuffer presentModes;
}
