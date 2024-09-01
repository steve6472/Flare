package steve6472.volkaniums.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineMultisampleStateCreateInfo;

import static org.lwjgl.vulkan.VK10.VK_SAMPLE_COUNT_1_BIT;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class MultisampleInfo
{
    boolean sampleShadingEnable = false;
    float minSampleShading = 1.0f;
    int rasterizationSamples = VK_SAMPLE_COUNT_1_BIT;

    VkPipelineMultisampleStateCreateInfo createInfo(MemoryStack stack)
    {
        VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
        multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        multisampling.sampleShadingEnable(sampleShadingEnable);
        multisampling.minSampleShading(minSampleShading);
        multisampling.rasterizationSamples(rasterizationSamples);

        return multisampling;
    }
}
