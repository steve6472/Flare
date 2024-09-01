package steve6472.volkaniums.pipeline;

import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class ColorBlendAttachment
{
    int writeMask;
    boolean blendEnable;

    void create(VkPipelineColorBlendAttachmentState state)
    {
        state.colorWriteMask(writeMask);
        state.blendEnable(blendEnable);
    }
}
