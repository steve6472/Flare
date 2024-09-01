package steve6472.volkaniums.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;
import org.lwjgl.vulkan.VkPipelineColorBlendStateCreateInfo;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_LOGIC_OP_COPY;
import static org.lwjgl.vulkan.VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class ColorBlendInfo
{
    List<ColorBlendAttachment> blends = new ArrayList<>();
    boolean logicOpEnable;
    int logicOp;
    float[] blendConstants;

    private VkPipelineColorBlendAttachmentState.Buffer createBuffer(MemoryStack stack)
    {
        VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(blends.size(), stack);
        for (int i = 0; i < blends.size(); i++)
        {
            ColorBlendAttachment blend = blends.get(i);
            VkPipelineColorBlendAttachmentState state = colorBlendAttachment.get(i);
            blend.create(state);
        }

        return colorBlendAttachment;
    }

    public VkPipelineColorBlendStateCreateInfo createInfo(MemoryStack stack)
    {
        VkPipelineColorBlendAttachmentState.Buffer buffer = createBuffer(stack);

        VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
        colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        colorBlending.logicOpEnable(false);
        colorBlending.logicOp(VK_LOGIC_OP_COPY);
        colorBlending.pAttachments(buffer);
        colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

        return colorBlending;
    }
}
