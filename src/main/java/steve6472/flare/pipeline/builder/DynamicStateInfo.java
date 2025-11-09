package steve6472.flare.pipeline.builder;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineDynamicStateCreateInfo;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 11/9/2025
 * Project: Flare <br>
 */
public class DynamicStateInfo
{
    List<Integer> states = new ArrayList<>(4);

    VkPipelineDynamicStateCreateInfo createInfo(MemoryStack stack)
    {
        VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack);
        dynamicState.sType(VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO);
        dynamicState.pDynamicStates(stack.ints(states.stream().mapToInt(i -> i).toArray()));
        return dynamicState;
    }
}
