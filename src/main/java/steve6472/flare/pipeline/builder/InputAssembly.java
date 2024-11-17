package steve6472.flare.pipeline.builder;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineInputAssemblyStateCreateInfo;

import static org.lwjgl.vulkan.VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
class InputAssembly
{
    int topology = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
    boolean primitiveRestartEnable = false;

    VkPipelineInputAssemblyStateCreateInfo createInfo(MemoryStack stack)
    {
        VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
        inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        inputAssembly.topology(topology);
        inputAssembly.primitiveRestartEnable(primitiveRestartEnable);
        return inputAssembly;
    }
}
