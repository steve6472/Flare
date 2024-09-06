package steve6472.volkaniums.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.volkaniums.FrameInfo;
import steve6472.volkaniums.pipeline.Pipeline;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public abstract class RenderSystem
{
    public final Pipeline pipeline;
    public final VkDevice device;

    public RenderSystem(VkDevice device, Pipeline pipeline)
    {
        this.device = device;
        this.pipeline = pipeline;
    }

    public abstract long[] setLayouts();

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);
    public abstract void cleanup();
}
