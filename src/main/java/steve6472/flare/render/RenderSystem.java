package steve6472.flare.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.MasterRenderer;
import steve6472.flare.pipeline.Pipeline;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.struct.type.StructVertex;
import steve6472.flare.vr.VrData;
import steve6472.flare.vr.VrRenderPass;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public abstract class RenderSystem
{
    private final MasterRenderer masterRenderer;

    private final Pipeline pipeline;
    private final Pipeline vrPipeline;
    public final VkDevice device;

    public RenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        this.masterRenderer = masterRenderer;
        this.pipeline = new Pipeline(pipeline);
        this.vrPipeline = VrData.VR_ON ? new Pipeline(pipeline) : null;
        this.device = masterRenderer.getDevice();
    }

    public Pipeline pipeline()
    {
        if (masterRenderer.getVrData().vrRenderPass != VrRenderPass.NONE)
            return vrPipeline;
        return pipeline;
    }

    protected StructVertex vertex()
    {
        return pipeline().vertex();
    }

    public Pipeline _getPipeline()
    {
        return pipeline;
    }

    public Pipeline _getVrPipeline()
    {
        return vrPipeline;
    }

    public MasterRenderer getMasterRenderer()
    {
        return masterRenderer;
    }

    public abstract long[] setLayouts();

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);
    public void postFrame() {}
    public abstract void cleanup();
}
