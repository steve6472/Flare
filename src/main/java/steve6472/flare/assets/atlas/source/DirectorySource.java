package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.module.Module;
import steve6472.core.module.ResourceCrawl;
import steve6472.core.registry.Key;
import steve6472.flare.FlareParts;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public record DirectorySource(String source, String prefix) implements Source
{
    public static final String DEFAULT_REFIX = "";

    public static final Codec<DirectorySource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("source").forGetter(DirectorySource::source),
        Codec.STRING.optionalFieldOf("prefix", DEFAULT_REFIX).forGetter(DirectorySource::prefix)
    ).apply(instance, DirectorySource::new));

    @Override
    public SourceType<?> getType()
    {
        return SourceType.DIRECTORY;
    }

    @Override
    public Collection<SourceResult> load(Module module, String namespace)
    {
        List<SourceResult> results = new ArrayList<>();
        File sourceFolder = new File(new File(new File(module.getRootFolder(), namespace), FlareParts.TEXTURES.path()), source);

        ResourceCrawl.crawl(sourceFolder, true, (filePath, id) ->
        {
            if (filePath.isFile() && filePath.getName().endsWith(".png"))
            {
                results.add(new SourceResult(filePath, Key.withNamespace(namespace, prefix + id)));
            }
        });
        return results;
    }
}
