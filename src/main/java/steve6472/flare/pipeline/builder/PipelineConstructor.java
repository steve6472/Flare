package steve6472.flare.pipeline.builder;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import steve6472.flare.pipeline.Pipeline;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
@FunctionalInterface
public interface PipelineConstructor
{
    Pipeline build(VkDevice device, VkExtent2D extent, long renderPass, long... setLayouts);
}
