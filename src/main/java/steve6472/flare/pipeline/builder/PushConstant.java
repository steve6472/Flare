package steve6472.flare.pipeline.builder;

import org.lwjgl.vulkan.VkPushConstantRange;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
class PushConstant
{
    int stageFlags;
    int offset;
    int size;

    public void create(VkPushConstantRange range)
    {
        range.stageFlags(stageFlags);
        range.offset(offset);
        range.size(size);
    }
}
