package steve6472.volkaniums.core;

import org.lwjgl.vulkan.VkCommandBuffer;
import steve6472.volkaniums.Camera;

/**
 * Created by steve6472
 * Date: 8/24/2024
 * Project: Volkaniums <br>
 */
public class FrameInfo
{
    int frameIndex;
    float frameTime;
    VkCommandBuffer commandBuffer;
    Camera camera;

    public int frameIndex() { return frameIndex; }
    public float frameTime() { return frameTime; }
    public VkCommandBuffer commandBuffer() { return commandBuffer; }
    public Camera camera() { return camera; }
}
