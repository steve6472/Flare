package steve6472.flare;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import steve6472.core.log.Log;
import steve6472.flare.vr.VrData;
import steve6472.flare.vr.VrUtil;

import java.nio.IntBuffer;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.vulkan.KHRShaderDrawParameters.VK_KHR_SHADER_DRAW_PARAMETERS_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Flare <br>
 */
public class PhysicalDevicePicker
{
    private static final Logger LOGGER = Log.getLogger(PhysicalDevicePicker.class);

    public static final Set<String> DEVICE_EXTENSIONS = Stream.of(VK_KHR_SWAPCHAIN_EXTENSION_NAME, VK_KHR_SHADER_DRAW_PARAMETERS_EXTENSION_NAME)
        .collect(Collectors.toSet());

    public static VkPhysicalDeviceLimits limits;

    public static VkPhysicalDevice pickPhysicalDevice(VkInstance instance, long surface, Collection<String> deviceExtensions)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer deviceCount = stack.ints(0);

            VK13.vkEnumeratePhysicalDevices(instance, deviceCount, null);

            if (deviceCount.get(0) == 0)
            {
                throw new RuntimeException(ErrorCode.NO_VULKAN_GPU.format());
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));

            VK13.vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);

            // Find first suitable GPU
            // TODO: Make this selectable by user
            for (int i = 0; i < ppPhysicalDevices.capacity(); i++)
            {
                VkPhysicalDevice device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);

                if (isDeviceSuitable(device, surface, deviceExtensions))
                {
                    VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.malloc();
                    VK13.vkGetPhysicalDeviceProperties(device, deviceProperties);
                    limits = deviceProperties.limits();
                    // TODO: actually use the value, don't hardcode stuff man...
//                    LOGGER.warning("alignment: " + limits.minUniformBufferOffsetAlignment()); // returns 64 on my device :)
//                    LOGGER.warning("nonCoherentAtomSize: " + limits.nonCoherentAtomSize()); // Returns 256 on my device
                    LOGGER.finer("Selected GPU: " + deviceProperties.deviceNameString());
                    return device;
                }
            }

            throw new RuntimeException(ErrorCode.NO_SUITABLE_GPU.format());
        }
    }

    private static boolean isDeviceSuitable(VkPhysicalDevice device, long surface, Collection<String> deviceExtensions)
    {
        // This does nothing, just for me to check stuff lol
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack);
            VK13.vkGetPhysicalDeviceFeatures(device, deviceFeatures);
        }

        QueueFamilyIndices indices = QueueFamilyIndices.findQueueFamilies(device, surface);

        boolean extensionsSupported = checkDeviceExtensionSupport(device, deviceExtensions);
        boolean swapChainAdequate = false;

        if (extensionsSupported)
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                SwapChainSupportDetails swapChainSupport = SwapChain.querySwapChainSupport(device, stack, surface);
                swapChainAdequate = swapChainSupport.formats.hasRemaining() && swapChainSupport.presentModes.hasRemaining();
            }
        }

        return indices.isComplete() && extensionsSupported && swapChainAdequate;
    }

    private static boolean checkDeviceExtensionSupport(VkPhysicalDevice device, Collection<String> deviceExtensions)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer extensionCount = stack.ints(0);

            VK13.vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0), stack);

            VK13.vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, availableExtensions);

            Collection<String> extensions = new HashSet<>(deviceExtensions);

            if (VrData.VR_ON)
            {
                String requiredExtensions = VrUtil.getRequiredExtensions(device.address());
                Collections.addAll(extensions, requiredExtensions.split(" "));
            }

            return availableExtensions
                .stream()
                .map(VkExtensionProperties::extensionNameString)
                .collect(Collectors.toSet())
                .containsAll(extensions);
        }
    }
}
