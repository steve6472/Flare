package steve6472.flare.render.common;

import steve6472.flare.assets.TextureSampler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public class CommonBuilder
{
    public List<CommonEntry> entries = new ArrayList<>();

    public static CommonBuilder create()
    {
        return new CommonBuilder();
    }

    public CommonBuilder entryImage(TextureSampler sampler)
    {
        entries.add(new EntrySampler(sampler));
        return this;
    }

    public CommonBuilder entrySBO(int instanceSize, int memoryPropertyFlags, int stage)
    {
        entries.add(new EntrySBO(instanceSize, memoryPropertyFlags, stage));
        return this;
    }

    public CommonBuilder vertexBuffer(int instanceSize, int instanceCount, int memoryPropertyFlags)
    {
        entries.add(new EntryVertexBuffer(instanceSize, instanceCount, memoryPropertyFlags));
        return this;
    }

    @Override
    public String toString()
    {
        return "CommonBuilder{" + "entries=" + entries + '}';
    }
}
