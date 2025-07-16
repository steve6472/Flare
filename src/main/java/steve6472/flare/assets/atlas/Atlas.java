package steve6472.flare.assets.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.registry.Key;
import steve6472.core.registry.Keyable;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.assets.atlas.source.Source;
import steve6472.flare.assets.atlas.source.SourceResult;
import steve6472.flare.core.Flare;
import steve6472.flare.ui.textures.SpriteEntry;

import java.util.*;

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
    /// Set from AtlasLoader
    Key key;

    private final Map<Key, SpriteEntry> sprites = new HashMap<>();
    // Set from SpriteLoader
    TextureSampler sampler = null;

    private Atlas(List<Source> sources)
    {
        this.sources = sources;
    }

    void mergeWith(Atlas other)
    {
        sources.addAll(other.sources);
    }

    void loadSprites()
    {
        // Set to prevent duplicates
        Set<SourceResult> toLoad = new LinkedHashSet<>();

        Flare.getModuleManager().iterateWithNamespaces((module, namespace) ->
        {
            for (Source source : sources)
            {
                Collection<SourceResult> load = source.load(module, namespace);
                for (SourceResult sourceResult : load)
                {
                    if (!sourceResult.file().isFile())
                        throw new RuntimeException("Source did not return a file");
                    if (!sourceResult.file().getName().endsWith(".png"))
                        throw new RuntimeException("Source returned a non-.png file");
                    toLoad.add(sourceResult);
                }
            }
        });

        sprites.putAll(SpriteLoader.loadFromAtlas(this, toLoad));
    }

    /// @return immutable copy
    public List<Source> getSources()
    {
        return List.copyOf(sources);
    }

    /// @return immutable copy
    public Map<Key, SpriteEntry> getSprites()
    {
        return Map.copyOf(sprites);
    }

    public TextureSampler getSampler()
    {
        return sampler;
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
