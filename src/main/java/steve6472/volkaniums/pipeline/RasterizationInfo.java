package steve6472.volkaniums.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineRasterizationStateCreateInfo;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class RasterizationInfo
{
    boolean depthClampEnable = false;
    boolean rasterizerDiscardEnable = false;
    boolean depthBiasEnable = false;

    int polygonMode = VK_POLYGON_MODE_FILL;
    int cullMode = VK_CULL_MODE_BACK_BIT;
    int frontFace = VK_FRONT_FACE_CLOCKWISE;

    float lineWidth = 1.0f;

    VkPipelineRasterizationStateCreateInfo createInfo(MemoryStack stack)
    {
        VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
        rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        rasterizer.depthClampEnable(depthClampEnable);
        rasterizer.rasterizerDiscardEnable(rasterizerDiscardEnable);
        rasterizer.polygonMode(polygonMode);
        rasterizer.lineWidth(lineWidth);
        rasterizer.cullMode(cullMode);
        rasterizer.frontFace(frontFace);
        rasterizer.depthBiasEnable(depthBiasEnable);

        return rasterizer;
    }
}
