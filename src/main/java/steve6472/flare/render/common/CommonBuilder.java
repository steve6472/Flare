package steve6472.flare.render.common;

import steve6472.core.registry.Holder;
import steve6472.flare.assets.TextureSampler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by steve6472
 * Date: 9/7/2025
 * Project: Flare <br>
 */
public class CommonBuilder
{
    public List<CommonEntry> entries = new ArrayList<>();
    public Consumer<FlightFrame> postCreation;

    public static CommonBuilder create()
    {
        return new CommonBuilder();
    }

    public CommonBuilder entryImage(Holder<TextureSampler> sampler)
    {
        entries.add(new EntrySampler(sampler));
        return this;
    }

    public CommonBuilder entryImages(Holder<TextureSampler>... sampler)
    {
        entries.add(new EntrySamplers(sampler));
        return this;
    }

    public CommonBuilder entrySBO(int instanceSize, int memoryPropertyFlags, int stage)
    {
        entries.add(new EntrySBO(instanceSize, memoryPropertyFlags, stage));
        return this;
    }

    public CommonBuilder entryUBO(int instanceSize, int memoryPropertyFlags, int stage)
    {
        entries.add(new EntryUBO(instanceSize, memoryPropertyFlags, stage, -1));
        return this;
    }

    public CommonBuilder entryUBO(int instanceSize, int memoryPropertyFlags, int stage, int rangeOverride)
    {
        entries.add(new EntryUBO(instanceSize, memoryPropertyFlags, stage, rangeOverride));
        return this;
    }

    public CommonBuilder vertexBuffer(int instanceSize, int instanceCount, int memoryPropertyFlags)
    {
        entries.add(new EntryVertexBuffer(instanceSize, instanceCount, memoryPropertyFlags));
        return this;
    }

    public CommonBuilder postCreation(Consumer<FlightFrame> postCreation)
    {
        this.postCreation = postCreation;
        return this;
    }

    @Override
    public String toString()
    {
        return "CommonBuilder{" + "entries=" + entries + '}';
    }
}
