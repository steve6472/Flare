package steve6472.flare.pipeline;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import steve6472.flare.FlareConstants;
import steve6472.flare.SwapChain;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.struct.type.StructVertex;

import java.nio.ByteBuffer;
import java.util.Objects;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
public class Pipeline
{
    private long pipeline;
    private long pipelineLayout;
    private StructVertex vertex;
    private final PipelineConstructor constructor;

    public Pipeline(PipelineConstructor constructor)
    {
        this.constructor = constructor;
        this.vertex = constructor.build(null, FlareConstants.NULL_EXTENT, 0).vertex();
    }

    public Pipeline(long pipeline, long pipelineLayout, StructVertex vertexInputInfo)
    {
        this.pipeline = pipeline;
        this.pipelineLayout = pipelineLayout;
        this.constructor = null;
        this.vertex = vertexInputInfo;
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
        Objects.requireNonNull(constructor, "Tried to rebuild a Pipeline from a temporary object!");

        Pipeline build = constructor.build(device, swapChain.swapChainExtent, swapChain.renderPass, setLayouts);
        pipeline = build.pipeline;
        pipelineLayout = build.pipelineLayout;
        vertex = build.vertex;
    }

    public void rebuild(VkDevice device, VkExtent2D extent, long renderPass, long... setLayouts)
    {
        Objects.requireNonNull(constructor, "Tried to rebuild a Pipeline from a temporary object!");

        Pipeline build = constructor.build(device, extent, renderPass, setLayouts);
        pipeline = build.pipeline;
        pipelineLayout = build.pipelineLayout;
        vertex = build.vertex;
    }

    public void cleanup(VkDevice device)
    {
        vkDestroyPipeline(device, pipeline, null);
        vkDestroyPipelineLayout(device, pipelineLayout, null);
    }

    public StructVertex vertex()
    {
        return vertex;
    }
}
