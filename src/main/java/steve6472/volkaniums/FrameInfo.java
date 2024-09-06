package steve6472.volkaniums;

import org.lwjgl.vulkan.VkCommandBuffer;

/**
 * Created by steve6472
 * Date: 8/24/2024
 * Project: Volkaniums <br>
 */
public class FrameInfo
{
    public int frameIndex;
    public float frameTime;
    public VkCommandBuffer commandBuffer;
    public Camera camera;
}
