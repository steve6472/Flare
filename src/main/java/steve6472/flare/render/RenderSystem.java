package steve6472.flare.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.MasterRenderer;
import steve6472.flare.pipeline.Pipeline;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.struct.type.StructVertex;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Flare <br>
 */
public abstract class RenderSystem
{
    private final MasterRenderer masterRenderer;

    private final Pipeline pipeline;
    public final VkDevice device;

    public RenderSystem(MasterRenderer masterRenderer, PipelineConstructor pipeline)
    {
        this.masterRenderer = masterRenderer;
        this.pipeline = new Pipeline(pipeline);
        this.device = masterRenderer.getDevice();
    }

    public Pipeline pipeline()
    {
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

    public MasterRenderer getMasterRenderer()
    {
        return masterRenderer;
    }

    public abstract long[] setLayouts();

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);
    public void postFrame() {}
    public abstract void cleanup();
}
