package steve6472.volkaniums.settings;

import org.lwjgl.vulkan.KHRSurface;
import steve6472.core.registry.StringValue;

/**
 * Created by steve6472
 * Date: 7/31/2024
 * Project: Volkaniums <br>
 */
public enum PresentMode implements StringValue
{
    IMMEDIATE("Immediate", KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR),
    MAILBOX("Mailbox", KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR),
    FIFO("FIFO", KHRSurface.VK_PRESENT_MODE_FIFO_KHR),
    FIFO_RELAXED("FIFO Relaxed", KHRSurface.VK_PRESENT_MODE_FIFO_RELAXED_KHR);

    private final String value;
    private final int vkValue;

    PresentMode(String value, int vkValue)
    {
        this.value = value;
        this.vkValue = vkValue;
    }

    public int getVkValue()
    {
        return vkValue;
    }

    @Override
    public String stringValue()
    {
        return value;
    }
}
