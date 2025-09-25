package steve6472.flare.render.common;

import steve6472.flare.VkBuffer;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public class FlightFrame
{
    public VkBuffer cameraUbo;
    Object[] userObjects;
    long descriptorSet;

    public VkBuffer getBuffer(int index)
    {
        return ((VkBuffer) userObjects[index]);
    }
}
