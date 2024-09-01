package steve6472.volkaniums.pipeline;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkGraphicsPipelineCreateInfo;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import steve6472.volkaniums.ErrorCode;
import steve6472.volkaniums.ShaderSPIRVUtils;
import steve6472.volkaniums.struct.type.StructVertex;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 9/1/2024
 * Project: Volkaniums <br>
 */
public final class PipelineBuilder
{
    private final VkDevice device;
    private StructVertex vertexInputInfo;

    private final ShadersInfo shadersInfo = new ShadersInfo();
    private final InputAssembly inputAssembly = new InputAssembly();
    private final ViewportInfo viewportInfo = new ViewportInfo();
    private final RasterizationInfo rasterizationInfo = new RasterizationInfo();
    private final MultisampleInfo multisampleInfo = new MultisampleInfo();
    private final DepthStencilInfo depthStencilInfo = new DepthStencilInfo();
    private final ColorBlendInfo colorBlendInfo = new ColorBlendInfo();
    private final PushConstantRange pushConstantRange = new PushConstantRange();

    private PipelineBuilder(VkDevice device)
    {
        this.device = device;
    }

    public ShadersBuilder shaders()
    {
        return new ShadersBuilder();
    }

    public PipelineBuilder vertexInputInfo(StructVertex vertexInputInfo)
    {
        this.vertexInputInfo = vertexInputInfo;
        return this;
    }

    public PipelineBuilder inputAssembly(int topology, boolean primitiveRestartEnable)
    {
        this.inputAssembly.topology = topology;
        this.inputAssembly.primitiveRestartEnable = primitiveRestartEnable;
        return this;
    }

    public ViewportBuilder viewport()
    {
        return new ViewportBuilder();
    }

    public RasterizationBuilder rasterization()
    {
        return new RasterizationBuilder();
    }

    public MultisampleBuilder multisampling()
    {
        return new MultisampleBuilder();
    }

    public DepthStencilBuilder depthStencil()
    {
        return new DepthStencilBuilder();
    }

    public ColorBlendBuilder colorBlend(boolean logicOpEnable, int logicOp, float... blendConstants)
    {
        this.colorBlendInfo.logicOpEnable = logicOpEnable;
        this.colorBlendInfo.logicOp = logicOp;
        this.colorBlendInfo.blendConstants = blendConstants;
        return new ColorBlendBuilder();
    }

    public PushConstantBuilder pushConstants()
    {
        return new PushConstantBuilder();
    }


    public static PipelineBuilder create(VkDevice device)
    {
        return new PipelineBuilder(device);
    }

    public Pipeline build(long renderPass, long... globalSetLayouts)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            long pipelineLayout = createPipelineLayout(stack, globalSetLayouts);

            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(shadersInfo.createInfo(device, stack));
            pipelineInfo.pVertexInputState(vertexInputInfo.createVertexInputInfo(stack));
            pipelineInfo.pInputAssemblyState(inputAssembly.createInfo(stack));
            pipelineInfo.pViewportState(viewportInfo.createInfo(stack));
            pipelineInfo.pRasterizationState(rasterizationInfo.createInfo(stack));
            pipelineInfo.pMultisampleState(multisampleInfo.createInfo(stack));
            pipelineInfo.pDepthStencilState(depthStencilInfo.createInfo(stack));
            pipelineInfo.pColorBlendState(colorBlendInfo.createInfo(stack));
            pipelineInfo.layout(pipelineLayout);
            pipelineInfo.renderPass(renderPass);
            pipelineInfo.subpass(0);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);

            LongBuffer pGraphicsPipeline = stack.callocLong(1);

            if (vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS)
            {
                throw new RuntimeException(ErrorCode.GRAPHICS_PIPELINE_CREATION.format());
            }

            shadersInfo.cleanup(device);

            return new Pipeline(pGraphicsPipeline.get(0), pipelineLayout);
        }
    }

    private long createPipelineLayout(MemoryStack stack, long... globalSetLayouts)
    {
        VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
        pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
        pipelineLayoutInfo.pPushConstantRanges(pushConstantRange.createRange(stack));
        pipelineLayoutInfo.pSetLayouts(stack.longs(globalSetLayouts));

        LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);

        if (vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS)
        {
            throw new RuntimeException(ErrorCode.PIPELINE_CREATION.format());
        }

        return pPipelineLayout.get(0);
    }

    public class ViewportBuilder extends NestedBuilder
    {
        public ViewportBuilder viewportDepths(float minDepth, float maxDepth)
        {
            viewportInfo.minDepth = minDepth;
            viewportInfo.maxDepth = maxDepth;
            return this;
        }

        public ViewportBuilder viewportBounds(float x, float y, float width, float height)
        {
            viewportInfo.x = x;
            viewportInfo.y = y;
            viewportInfo.width = width;
            viewportInfo.height = height;
            return this;
        }

        public ViewportBuilder scissorOffset(int x, int y)
        {
            viewportInfo.offsetX = x;
            viewportInfo.offsetY = y;
            return this;
        }

        public ViewportBuilder scissorExtent(VkExtent2D extent)
        {
            viewportInfo.extent = extent;
            return this;
        }
    }

    public class DepthStencilBuilder extends NestedBuilder
    {
        public DepthStencilBuilder bounds(float minDepthBound, float maxDepthBound, boolean depthBoundsTestEnable)
        {
            depthStencilInfo.minDepthBounds = minDepthBound;
            depthStencilInfo.maxDepthBounds = maxDepthBound;
            depthStencilInfo.depthBoundsTestEnable = depthBoundsTestEnable;
            return this;
        }

        public DepthStencilBuilder stencilTestEnable(boolean stencilTestEnable)
        {
            depthStencilInfo.stencilTestEnable = stencilTestEnable;
            return this;
        }

        public DepthStencilBuilder depthCompareOp(int depthCompareOp)
        {
            depthStencilInfo.depthCompareOp = depthCompareOp;
            return this;
        }

        public DepthStencilBuilder depthEnableFlags(boolean depthTestEnable, boolean depthWriteEnable)
        {
            depthStencilInfo.depthTestEnable = depthTestEnable;
            depthStencilInfo.depthWriteEnable = depthWriteEnable;
            return this;
        }
    }

    public class MultisampleBuilder extends NestedBuilder
    {
        public MultisampleBuilder sampleShading(boolean sampleShadingEnable)
        {
            multisampleInfo.sampleShadingEnable = sampleShadingEnable;
            return this;
        }

        public MultisampleBuilder sampleShading(boolean sampleShadingEnable, float minSampleShading)
        {
            multisampleInfo.sampleShadingEnable = sampleShadingEnable;
            multisampleInfo.minSampleShading = minSampleShading;
            return this;
        }

        public MultisampleBuilder rasterizationSamples(int rasterizationSamples)
        {
            multisampleInfo.rasterizationSamples = rasterizationSamples;
            return this;
        }
    }

    public class ShadersBuilder extends NestedBuilder
    {
        public ShadersBuilder addShader(ShaderSPIRVUtils.ShaderKind kind, String shaderFile, int stage)
        {
            Shader shader = new Shader();
            shader.kind = kind;
            shader.shaderFile = shaderFile;
            shader.stage = stage;
            shadersInfo.shaders.add(shader);
            return this;
        }

        /**
         * Default: "main"
         * @param newEntryPoint new entry point
         * @return builder
         */
        public ShadersBuilder changeEntryPoint(String newEntryPoint)
        {
            shadersInfo.shaders.getLast().entryPoint = newEntryPoint;
            return this;
        }
    }

    public class RasterizationBuilder extends NestedBuilder
    {
        public RasterizationBuilder polygonInfo(int polygonMode, int cullMode, int frontFace)
        {
            rasterizationInfo.polygonMode = polygonMode;
            rasterizationInfo.cullMode = cullMode;
            rasterizationInfo.frontFace = frontFace;
            return this;
        }

        public RasterizationBuilder lineWidth(float lineWidth)
        {
            rasterizationInfo.lineWidth = lineWidth;
            return this;
        }

        public RasterizationBuilder flags(boolean depthClampEnable, boolean rasterizerDiscardEnable, boolean depthBiasEnable)
        {
            rasterizationInfo.depthClampEnable = depthClampEnable;
            rasterizationInfo.rasterizerDiscardEnable = rasterizerDiscardEnable;
            rasterizationInfo.depthBiasEnable = depthBiasEnable;
            return this;
        }
    }

    public class ColorBlendBuilder extends NestedBuilder
    {
        public ColorBlendBuilder attachment(int writeMask, boolean blendEnable)
        {
            ColorBlendAttachment attachment = new ColorBlendAttachment();
            attachment.writeMask = writeMask;
            attachment.blendEnable = blendEnable;
            colorBlendInfo.blends.add(attachment);
            return this;
        }
    }

    public class PushConstantBuilder extends NestedBuilder
    {
        public PushConstantBuilder constant(int stageFlags, int offset, int size)
        {
            PushConstant constant = new PushConstant();
            constant.stageFlags = stageFlags;
            constant.offset = offset;
            constant.size = size;
            pushConstantRange.constants.add(constant);
            return this;
        }
    }

    public abstract class NestedBuilder
    {
        private NestedBuilder() {}

        public PipelineBuilder done()
        {
            return PipelineBuilder.this;
        }
    }
}
