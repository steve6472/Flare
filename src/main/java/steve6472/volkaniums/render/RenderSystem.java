package steve6472.volkaniums.render;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.volkaniums.FrameInfo;

/**
 * Created by steve6472
 * Date: 8/31/2024
 * Project: Volkaniums <br>
 */
public abstract class RenderSystem
{
    VkDevice device;

    public RenderSystem(VkDevice device)
    {
        this.device = device;
    }

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);
    public abstract void cleanup();
}
