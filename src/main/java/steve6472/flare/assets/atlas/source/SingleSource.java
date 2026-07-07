package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.module.Module;
import steve6472.core.registry.Key;
import steve6472.flare.FlareConstants;
import steve6472.flare.FlareParts;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public record SingleSource(String resource, Optional<Key> name) implements Source
{
    public static final MapCodec<SingleSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.STRING.fieldOf("resource").forGetter(SingleSource::resource),
        FlareConstants.FLARE_KEY_CODEC.optionalFieldOf("name").forGetter(SingleSource::name)
    ).apply(instance, SingleSource::new));

    public SingleSource
    {
        if (!resource.contains(":"))
            throw new IllegalArgumentException("resource has to specify namespace");
    }

    @Override
    public Collection<SourceResult> load(Module module, String namespace)
    {
        Key parse = Key.parse(FlareConstants.NAMESPACE, resource);

        if (!parse.namespace().equals(namespace))
            return List.of();

        File sourceFile = new File(new File(new File(module.getRootFolder(), namespace), FlareParts.TEXTURES.path()), parse.id() + ".png");
        if (!sourceFile.exists())
            return List.of();

        return List.of(new SourceResult(sourceFile, name.orElse(parse)));
    }

    @Override
    public MapCodec<? extends Source> codec()
    {
        return CODEC;
    }
}
