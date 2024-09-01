package steve6472.volkaniums.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
public class ViewportInfo
{
    float x;
    float y;
    float width;
    float height;
    float minDepth;
    float maxDepth;

    int offsetX;
    int offsetY;

    VkExtent2D extent;

    public VkPipelineViewportStateCreateInfo createInfo(MemoryStack stack)
    {
        VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
        viewport.x(x);
        viewport.y(y);
        viewport.width(width);
        viewport.height(height);
        viewport.minDepth(minDepth);
        viewport.maxDepth(maxDepth);

        VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
        scissor.offset(VkOffset2D.calloc(stack).set(offsetX, offsetY));
        scissor.extent(extent);

        VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
        viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        viewportState.pViewports(viewport);
        viewportState.pScissors(scissor);

        return viewportState;
    }
}
