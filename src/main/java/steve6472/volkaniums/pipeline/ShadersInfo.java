package steve6472.volkaniums.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class ShadersInfo
{
    List<Shader> shaders = new ArrayList<>();

    VkPipelineShaderStageCreateInfo.Buffer createInfo(VkDevice device, MemoryStack stack)
    {
        VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(shaders.size(), stack);

        for (int i = 0; i < shaders.size(); i++)
        {
            VkPipelineShaderStageCreateInfo createInfo = shaderStages.get(i);
            shaders.get(i).create(device, createInfo, stack);
        }

        return shaderStages;
    }

    void cleanup(VkDevice device)
    {
        shaders.forEach(s -> s.cleanup(device));
    }
}
