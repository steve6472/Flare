package steve6472.volkaniums;

import java.nio.LongBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;

/**
 * Wraps the needed sync objects for an in flight frame
 * <p>
 * This frame's sync objects must be deleted manually
 */
public record SyncFrame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence)
{
    public LongBuffer pImageAvailableSemaphore()
    {
        return stackGet().longs(imageAvailableSemaphore);
    }

    public LongBuffer pRenderFinishedSemaphore()
    {
        return stackGet().longs(renderFinishedSemaphore);
    }

    public LongBuffer pFence()
    {
        return stackGet().longs(fence);
    }

    @Override
    public String toString()
    {
        return "Frame{" + "imageAvailableSemaphore=" + imageAvailableSemaphore + ", renderFinishedSemaphore=" + renderFinishedSemaphore + ", fence=" + fence + '}';
    }
}