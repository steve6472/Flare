package steve6472.volkaniums.pipeline;

import steve6472.volkaniums.ShaderSPIRVUtils;
import steve6472.volkaniums.struct.def.Push;
import steve6472.volkaniums.struct.def.Vertex;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
public interface Pipelines
{
    PipelineConstructor BASIC = (device, swapChain, globalSetLayouts) -> PipelineBuilder.create(device)
        .shaders()
            .addShader(ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER, "shaders/shader_base.vert", VK_SHADER_STAGE_VERTEX_BIT)
            .addShader(ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER, "shaders/shader_base.frag", VK_SHADER_STAGE_FRAGMENT_BIT)
            .done()
        .vertexInputInfo(Vertex.POS3F_COL3F_UV)
        .inputAssembly(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST, false)
        .viewport()
            .viewportBounds(0.0f, 0.0f, swapChain.swapChainExtent.width(), swapChain.swapChainExtent.height())
            .viewportDepths(0.0f, 1.0f)
            .scissorOffset(0, 0)
            .scissorExtent(swapChain.swapChainExtent)
            .done()
        .rasterization()
            .flags(false, false, false)
            .lineWidth(1.0f)
            .polygonInfo(VK_POLYGON_MODE_FILL, VK_CULL_MODE_BACK_BIT, VK_FRONT_FACE_CLOCKWISE)
            .done()
        .multisampling()
            .sampleShading(false)
            .rasterizationSamples(VK_SAMPLE_COUNT_1_BIT)
            .done()
        .depthStencil()
            .depthEnableFlags(true, true)
            .depthCompareOp(VK_COMPARE_OP_LESS)
            .bounds(0.0f, 1.0f, false)
            .stencilTestEnable(false)
            .done()
        .colorBlend(true, VK_LOGIC_OP_COPY, 0f, 0f, 0f, 0f)
            .attachment(VK_COLOR_COMPONENT_R_BIT | VK_COLOR_COMPONENT_G_BIT | VK_COLOR_COMPONENT_B_BIT | VK_COLOR_COMPONENT_A_BIT, false)
            .done()
        .pushConstants()
            .constant(VK_SHADER_STAGE_VERTEX_BIT | VK_SHADER_STAGE_FRAGMENT_BIT, 0, Push.PUSH.sizeof())
            .done()
        .build(swapChain.renderPass, globalSetLayouts);
}
