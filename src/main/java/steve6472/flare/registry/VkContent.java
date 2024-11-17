package steve6472.flare.registry;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.flare.Commands;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
@FunctionalInterface
public interface VkContent<T>
{
    T apply(VkDevice device, Commands commands, VkQueue graphicsQueue);
}