package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.core.module.Module;
import steve6472.core.util.ExtraCodecs;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public record FilterSource(Optional<Pattern> namespace, Optional<Pattern> path) implements Source
{
    public static final Codec<FilterSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(FilterSource::namespace),
        ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(FilterSource::path)
    ).apply(instance, FilterSource::new));

    @Override
    public Collection<SourceResult> load(Module module, String namespace)
    {
        throw new UnsupportedOperationException("Unimplemented source type");
//        return List.of();
    }

    @Override
    public SourceType<?> getType()
    {
        return SourceType.FILTER;
    }
}
