package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
}
