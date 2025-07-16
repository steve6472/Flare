package steve6472.flare.assets.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.assets.atlas.source.Source;

import java.util.List;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public class Atlas implements Keyable
{
    public static final Codec<Atlas> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Source.CODEC.listOf().fieldOf("sources").forGetter(Atlas::getSources)
    ).apply(instance, Atlas::new));

    private final List<Source> sources;
    // set from AtlasLoader
    Key key;

    private Atlas(List<Source> sources)
    {
        this.sources = sources;
    }

    /// @return immutable copy
    public List<Source> getSources()
    {
        return List.copyOf(sources);
    }

    @Override
    public Key key()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "Atlas{" + "sources=" + sources + '}';
    }
}
