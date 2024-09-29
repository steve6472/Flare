package steve6472.volkaniums.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.MasterRenderer;
import steve6472.volkaniums.pipeline.Pipeline;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.vr.VrData;
import steve6472.volkaniums.vr.VrRenderPass;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
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
    public abstract void cleanup();
}
