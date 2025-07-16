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
import java.util.Optional;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public record SingleSource(String resource, Optional<String> name) implements Source
{
    public static final Codec<SingleSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("resource").forGetter(SingleSource::resource),
        Codec.STRING.optionalFieldOf("name").forGetter(SingleSource::name)
    ).apply(instance, SingleSource::new));

    @Override
    public SourceType<?> getType()
    {
        return SourceType.SINGLE;
    }

    @Override
    public Collection<SourceResult> load(Module module, String namespace)
    {
//        Key parse = Key.parse(namespace, resource);
//        File sourceFolder = new File(new File(new File(module.getRootFolder(), namespace), FlareParts.TEXTURES.path()), resource);

//        String replace = listFile.getAbsolutePath().replace("\\", "/");
//        String replace1 = startingDir.getAbsolutePath().replace("\\", "/");
//        String substring = replace.substring(replace1.length() + 1);
//        if (stripExtFromRel)
//            substring = substring.substring(0, substring.lastIndexOf('.'));

//        return List.of(new SourceResult(sourceFolder, null));
//        return List.of();
        throw new UnsupportedOperationException("Unimplemented source type");
    }
}
