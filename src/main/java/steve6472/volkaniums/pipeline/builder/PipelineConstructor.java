package steve6472.volkaniums.pipeline.builder;

import org.lwjgl.vulkan.VkDevice;
import steve6472.volkaniums.SwapChain;
import steve6472.volkaniums.pipeline.Pipeline;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
@FunctionalInterface
public interface PipelineConstructor
{
    Pipeline build(VkDevice device, SwapChain swapChain, long... setLayouts);
}
