package steve6472.volkaniums;

import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public class Renderer
{
    private final Window window;
    private final VkDevice device;

    public Renderer(Window window, VkDevice device)
    {
        this.window = window;
        this.device = device;
    }

    VkCommandBuffer beginFrame()
    {
        return null;
    }

    void endFrame()
    {

    }

    void beginSwapChainRenderPass(VkCommandBuffer commandBuffer)
    {

    }

    void endSwapChainRenderPass(VkCommandBuffer commandBuffer)
    {

    }
}