package steve6472.volkaniums.pipeline.builder;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import steve6472.volkaniums.ErrorCode;
import steve6472.volkaniums.ShaderSPIRVUtils;
import steve6472.volkaniums.util.Preconditions;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
class Shader
{
    ShaderSPIRVUtils.ShaderKind kind;
    String shaderFile;
    int stage;

    String entryPoint = "main";

    private ShaderSPIRVUtils.SPIRV spirv;
    private long shaderModule;

    void create(VkDevice device, VkPipelineShaderStageCreateInfo createInfo, MemoryStack stack)
    {
        Preconditions.checkNotNull(kind, "Shader kind has to be selected");
        Preconditions.checkNotNull(shaderFile, "Shader file has to be selected");

        spirv = ShaderSPIRVUtils.compileShaderFile(shaderFile, kind);
        shaderModule = createShaderModule(spirv.bytecode(), device);

        createInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        createInfo.stage(stage);
        createInfo.module(shaderModule);
        createInfo.pName(stack.UTF8(entryPoint));
    }

    void cleanup(VkDevice device)
    {
        vkDestroyShaderModule(device, shaderModule, null);
        spirv.free();
    }

    private long createShaderModule(ByteBuffer spirvCode, VkDevice device)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack);

            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);

            if (vkCreateShaderModule(device, createInfo, null, pShaderModule) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.SHADER_MODULE_CREATION.format());
            }

            return pShaderModule.get(0);
        }
    }
}
