package steve6472.volkaniums.pipeline;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import steve6472.volkaniums.SwapChain;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.util.Preconditions;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
public class Pipeline
{
    private long pipeline;
    private long pipelineLayout;
    private final PipelineConstructor constructor;

    public Pipeline(PipelineConstructor constructor)
    {
        this.constructor = constructor;
    }

    public Pipeline(long pipeline, long pipelineLayout)
    {
        this.pipeline = pipeline;
        this.pipelineLayout = pipelineLayout;
        this.constructor = null;
    }

    public void bind(VkCommandBuffer commandBuffer)
    {
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, pipeline);
    }

    public long pipeline()
    {
        return pipeline;
    }

    public long pipelineLayout()
    {
        return pipelineLayout;
    }

    public void rebuild(VkDevice device, SwapChain swapChain, long... setLayouts)
    {
        Preconditions.checkNotNull(constructor, "Tried to rebuild a Pipeline from a temporary object!");

        Pipeline build = constructor.build(device, swapChain.swapChainExtent, swapChain.renderPass, setLayouts);
        pipeline = build.pipeline;
        pipelineLayout = build.pipelineLayout;
    }

    public void rebuild(VkDevice device, VkExtent2D extent, long renderPass, long... setLayouts)
    {
        Preconditions.checkNotNull(constructor, "Tried to rebuild a Pipeline from a temporary object!");

        Pipeline build = constructor.build(device, extent, renderPass, setLayouts);
        pipeline = build.pipeline;
        pipelineLayout = build.pipelineLayout;
    }

    public void cleanup(VkDevice device)
    {
        vkDestroyPipeline(device, pipeline, null);
        vkDestroyPipelineLayout(device, pipelineLayout, null);
    }
}
