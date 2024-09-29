package steve6472.volkaniums;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;
import steve6472.core.log.Log;
import steve6472.volkaniums.settings.ValidationLevel;
import steve6472.volkaniums.settings.VisualSettings;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public final class VulkanValidation
{
    private static final Logger LOGGER = Log.getLogger("Validation");

    /// TODO: Replace with [VisualSettings#VALIDATION_LEVEL] [ValidationLevel#NONE]
    public static final boolean ENABLE_VALIDATION_LAYERS = true;

    private static final Set<String> VALIDATION_LAYERS;

    static
    {
        if (ENABLE_VALIDATION_LAYERS)
        {
            VALIDATION_LAYERS = new HashSet<>();
            VALIDATION_LAYERS.add("VK_LAYER_KHRONOS_validation");
        } else
        {
            // We are not going to use it, so we don't create it
            VALIDATION_LAYERS = null;
        }
    }

    public static Set<String> getValidationLayers()
    {
        if (VALIDATION_LAYERS == null)
            throw new RuntimeException(ErrorCode.VALIDATION_NOT_ENABLED.format());

        return VALIDATION_LAYERS;
    }

    public static boolean checkValidationLayerSupport()
    {
        if (VALIDATION_LAYERS == null)
            throw new RuntimeException(ErrorCode.VALIDATION_NOT_ENABLED.format());

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer layerCount = stack.ints(0);

            VK13.vkEnumerateInstanceLayerProperties(layerCount, null);

            VkLayerProperties.Buffer availableLayers = VkLayerProperties.malloc(layerCount.get(0), stack);

            VK13.vkEnumerateInstanceLayerProperties(layerCount, availableLayers);

            Set<String> availableLayerNames = availableLayers
                .stream()
                .map(VkLayerProperties::layerNameString)
                .collect(Collectors.toSet());

            return availableLayerNames.containsAll(VALIDATION_LAYERS);
        }
    }

    public static PointerBuffer getRequiredExtensions(MemoryStack stack)
    {
        PointerBuffer glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();

        if (ENABLE_VALIDATION_LAYERS)
        {
            PointerBuffer extensions = stack.mallocPointer(glfwExtensions.capacity() + 1);

            extensions.put(glfwExtensions);
            extensions.put(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));

            // Rewind the buffer before returning it to reset its position back to 0
            return extensions.rewind();
        }

        return glfwExtensions;
    }

    public static void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo)
    {
        debugCreateInfo.sType(EXTDebugUtils.VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
        debugCreateInfo.messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
        debugCreateInfo.messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT);
        debugCreateInfo.pfnUserCallback(VulkanValidation::debugCallback);
    }

    private static int debugCallback(int messageSeverity, int messageType, long pCallbackData, long pUserData)
    {
        VkDebugUtilsMessengerCallbackDataEXT callbackData = VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData);

        String type = "(General) ";

        if (messageType == EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT)
            type = "(General) ";
        else if (messageType == EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
            type = "(Validation) ";
        else if (messageType == EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
            type = "(Performance) ";

        if (messageSeverity >= EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT && VisualSettings.VALIDATION_LEVEL.get().ordinal() >= 1)
        {
            LOGGER.severe(type + callbackData.pMessageString());
        } else if (messageSeverity >= EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT && VisualSettings.VALIDATION_LEVEL.get().ordinal() >= 2)
        {
            LOGGER.warning(type + callbackData.pMessageString());
        } else if (messageSeverity >= EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT && VisualSettings.VALIDATION_LEVEL.get().ordinal() >= 3)
        {
            LOGGER.info(type + callbackData.pMessageString());
        } else if (messageSeverity >= EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT && VisualSettings.VALIDATION_LEVEL.get().ordinal() >= 4)
        {
            LOGGER.fine(type + callbackData.pMessageString());
        }

        return VK13.VK_FALSE;
    }

    public static int createDebugUtilsMessengerEXT(VkInstance instance, VkDebugUtilsMessengerCreateInfoEXT createInfo, VkAllocationCallbacks allocationCallbacks, LongBuffer pDebugMessenger)
    {
        if (VK13.vkGetInstanceProcAddr(instance, "vkCreateDebugUtilsMessengerEXT") != MemoryUtil.NULL)
        {
            return EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, createInfo, allocationCallbacks, pDebugMessenger);
        }

        return VK13.VK_ERROR_EXTENSION_NOT_PRESENT;
    }

    public static void destroyDebugUtilsMessengerEXT(VkInstance instance, long debugMessenger, VkAllocationCallbacks allocationCallbacks)
    {
        if (VK13.vkGetInstanceProcAddr(instance, "vkDestroyDebugUtilsMessengerEXT") != MemoryUtil.NULL)
        {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, allocationCallbacks);
        }
    }

    public static long setupDebugMessenger(VkInstance instance)
    {
        if (!ENABLE_VALIDATION_LAYERS)
        {
            return MemoryUtil.NULL;
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);

            populateDebugMessengerCreateInfo(createInfo);

            LongBuffer pDebugMessenger = stack.longs(VK13.VK_NULL_HANDLE);

            if (createDebugUtilsMessengerEXT(instance, createInfo, null, pDebugMessenger) != VK13.VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.SETUP_DEBUG_MESSANGER.format());
            }

            return pDebugMessenger.get(0);
        }
    }

}
