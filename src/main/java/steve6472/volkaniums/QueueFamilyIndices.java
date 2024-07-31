package steve6472.volkaniums;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSurface;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;

import java.nio.IntBuffer;
import java.util.stream.IntStream;

/**
 * Created by steve6472
 * Date: 7/30/2024
 * Project: Volkaniums <br>
 */
public class QueueFamilyIndices
{
    // We use Integer to use null as the empty value
    public Integer graphicsFamily;
    public Integer presentFamily;

    public boolean isComplete()
    {
        return graphicsFamily != null && presentFamily != null;
    }

    public int[] unique()
    {
        return IntStream.of(graphicsFamily, presentFamily).distinct().toArray();
    }

    public int[] array()
    {
        return new int[] {graphicsFamily, presentFamily};
    }

    public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface)
    {
        QueueFamilyIndices indices = new QueueFamilyIndices();

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer queueFamilyCount = stack.ints(0);

            VK13.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);

            VK13. vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK13.VK_FALSE);

            for (int i = 0; i < queueFamilies.capacity() || !indices.isComplete(); i++)
            {

                if ((queueFamilies.get(i).queueFlags() & VK13.VK_QUEUE_GRAPHICS_BIT) != 0)
                {
                    indices.graphicsFamily = i;
                }

                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);

                if (presentSupport.get(0) == VK13.VK_TRUE)
                {
                    indices.presentFamily = i;
                }
            }

            return indices;
        }
    }
}
