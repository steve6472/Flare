package steve6472.flare.registry;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.flare.Commands;

/**
 * Created by steve6472
 * Date: 6/7/2026
 * Project: Flare <br>
 *
 */
public record VkSetup(VkDevice device, Commands commands, VkQueue graphicsQueue)
{
}
