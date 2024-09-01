package steve6472.volkaniums.pipeline;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import steve6472.volkaniums.SwapChain;
import steve6472.volkaniums.util.Preconditions;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
public class Pipeline
{
    private long graphicsPipeline;
    private long pipelineLayout;
    private final PipelineConstructor constructor;

    public Pipeline(PipelineConstructor constructor)
    {
        this.constructor = constructor;
    }

    Pipeline(long graphicsPipeline, long pipelineLayout)
    {
        this.graphicsPipeline = graphicsPipeline;
        this.pipelineLayout = pipelineLayout;
        this.constructor = null;
    }

    public void bind(VkCommandBuffer commandBuffer)
    {
        vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
    }

    public long graphicsPipeline()
    {
        return graphicsPipeline;
    }

    public long pipelineLayout()
    {
        return pipelineLayout;
    }

    public void rebuild(VkDevice device, SwapChain swapChain, long... globalSetLayouts)
    {
        Preconditions.checkNotNull(constructor, "Tried to rebuild a Pipeline from a temporary object!");

        Pipeline build = constructor.build(device, swapChain, globalSetLayouts);
        graphicsPipeline = build.graphicsPipeline;
        pipelineLayout = build.pipelineLayout;
    }
}
