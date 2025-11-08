package steve6472.flare.pipeline.shader;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkPipelineShaderStageCreateInfo;
import org.lwjgl.vulkan.VkShaderModuleCreateInfo;
import steve6472.core.util.Preconditions;
import steve6472.flare.ErrorCode;
import steve6472.flare.ShaderSPIRVUtils;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Flare <br>
 */
public class Shader
{
    private final ShaderId shaderId;

    Shader(ShaderId shaderId)
    {
        this.shaderId = shaderId;
    }

    private ShaderSPIRVUtils.SPIRV spirv;
    private long shaderModule;

    public void create(VkDevice device, VkPipelineShaderStageCreateInfo createInfo, MemoryStack stack)
    {
        if (spirv == null)
        {
            spirv = ShaderSPIRVUtils.compileShaderFile(shaderId.file(), shaderId.kind());
            shaderModule = createShaderModule(spirv.bytecode(), device);
        }

        createInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        createInfo.stage(shaderId.stage());
        createInfo.module(shaderModule);
        createInfo.pName(stack.UTF8(shaderId.entryPoint()));
    }

    public void cleanup(VkDevice device)
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
