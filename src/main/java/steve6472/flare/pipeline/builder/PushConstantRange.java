package steve6472.flare.pipeline.builder;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPushConstantRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
class PushConstantRange
{
    List<PushConstant> constants = new ArrayList<>();

    VkPushConstantRange.Buffer createRange(MemoryStack stack)
    {
        VkPushConstantRange.Buffer pushConstantRange = VkPushConstantRange.calloc(constants.size(), stack);

        for (int i = 0; i < constants.size(); i++)
        {
            VkPushConstantRange range = pushConstantRange.get(i);
            constants.get(i).create(range);
        }

        return pushConstantRange;
    }
}
