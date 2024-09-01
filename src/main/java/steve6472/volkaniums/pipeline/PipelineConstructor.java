package steve6472.volkaniums.pipeline;

import org.lwjgl.vulkan.VkDevice;
import steve6472.volkaniums.SwapChain;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
@FunctionalInterface
public interface PipelineConstructor
{
    Pipeline build(VkDevice device, SwapChain swapChain, long... globalSetLayouts);
}
