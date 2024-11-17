package steve6472.flare.pipeline.builder;

import org.lwjgl.vulkan.VkPipelineColorBlendAttachmentState;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
class ColorBlendAttachment
{
    int writeMask;
    boolean blendEnable;

    int srcColorBlendFactor = VK_BLEND_FACTOR_ONE;
    int dstColorBlendFactor = VK_BLEND_FACTOR_ZERO;
    int colorBlendOp = VK_BLEND_OP_ADD;

    int srcAlphaBlendFactor = VK_BLEND_FACTOR_ONE;
    int dstAlphaBlendFactor = VK_BLEND_FACTOR_ZERO;
    int alphaBlendOp = VK_BLEND_OP_ADD;

    void create(VkPipelineColorBlendAttachmentState state)
    {
        state.colorWriteMask(writeMask);
        state.blendEnable(blendEnable);
        state.srcColorBlendFactor(srcColorBlendFactor);
        state.dstColorBlendFactor(dstColorBlendFactor);
        state.colorBlendOp(colorBlendOp);
        state.srcAlphaBlendFactor(srcAlphaBlendFactor);
        state.dstAlphaBlendFactor(dstAlphaBlendFactor);
        state.alphaBlendOp(alphaBlendOp);
    }
}
