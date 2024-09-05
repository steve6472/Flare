package steve6472.volkaniums.pipeline.builder;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineDepthStencilStateCreateInfo;

import static org.lwjgl.vulkan.VK10.VK_COMPARE_OP_LESS;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class DepthStencilInfo
{
    boolean depthTestEnable = true;
    boolean depthWriteEnable = true;
    boolean depthBoundsTestEnable = false;
    boolean stencilTestEnable = false;

    int depthCompareOp = VK_COMPARE_OP_LESS;

    float minDepthBounds = 0.0f;
    float maxDepthBounds = 1.0f;

    VkPipelineDepthStencilStateCreateInfo createInfo(MemoryStack stack)
    {
        VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
        depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
        depthStencil.depthTestEnable(depthTestEnable);
        depthStencil.depthWriteEnable(depthWriteEnable);
        depthStencil.depthCompareOp(depthCompareOp);
        depthStencil.depthBoundsTestEnable(depthBoundsTestEnable);
        depthStencil.minDepthBounds(minDepthBounds);
        depthStencil.maxDepthBounds(maxDepthBounds);
        depthStencil.stencilTestEnable(stencilTestEnable);
        return depthStencil;
    }
}
