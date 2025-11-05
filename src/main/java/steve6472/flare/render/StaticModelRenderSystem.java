package steve6472.flare.render;

import org.lwjgl.system.MemoryStack;
import steve6472.flare.*;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.blockbench.ErrorModel;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.common.CommonBuilder;
import steve6472.flare.render.common.CommonRenderSystem;
import steve6472.flare.render.common.FlightFrame;
import steve6472.flare.struct.Struct;
import steve6472.flare.struct.def.Push;
import steve6472.flare.struct.def.SBO;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public class StaticModelRenderSystem extends CommonRenderSystem
{
    private final SBOTransfromArray<Model> transfromArray = new SBOTransfromArray<>(ErrorModel.VK_STATIC_INSTANCE);
    private final StaticModelRenderImpl renderImpl;

    public StaticModelRenderSystem(MasterRenderer masterRenderer, StaticModelRenderImpl renderImpl, PipelineConstructor pipeline)
    {
        super(masterRenderer, pipeline, CommonBuilder.create()
            .entryImage(FlareRegistries.ATLAS.get(FlareConstants.ATLAS_BLOCKBENCH).getSampler())
            .entrySBO(SBO.BLOCKBENCH_STATIC_TRANSFORMATIONS.sizeof(), VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, VK_SHADER_STAGE_VERTEX_BIT));

        this.renderImpl = renderImpl;
        this.renderImpl.init(transfromArray);
    }

    @Override
    protected void render(FlightFrame flightFrame, FrameInfo frameInfo, MemoryStack stack)
    {
        int totalIndex = 0;
        for (var area : transfromArray.getAreas())
        {
            if (area.toRender == 0)
                continue;

            Struct offset = Push.STATIC_TRANSFORM_OFFSET.create(totalIndex);
            Push.STATIC_TRANSFORM_OFFSET.push(offset, frameInfo.commandBuffer(), pipeline().pipelineLayout(), VK_SHADER_STAGE_VERTEX_BIT, 0);
            area.modelType.bind(frameInfo.commandBuffer());
            area.modelType.draw(frameInfo.commandBuffer(), area.toRender);
            totalIndex += area.toRender;
        }
    }

    @Override
    protected void updateData(FlightFrame flightFrame, FrameInfo frameInfo)
    {
        VkBuffer buffer = flightFrame.getBuffer(1);

        transfromArray.start();
        renderImpl.updateTransformArray(transfromArray, frameInfo);

        var sbo = SBO.BLOCKBENCH_STATIC_TRANSFORMATIONS.create(transfromArray.getTransformsArray());
        buffer.writeToBuffer(SBO.BLOCKBENCH_STATIC_TRANSFORMATIONS::memcpy, sbo);
    }
}
