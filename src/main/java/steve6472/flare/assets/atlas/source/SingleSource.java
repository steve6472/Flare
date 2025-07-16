package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public record SingleSource(String resource) implements Source
{
    public static final Codec<SingleSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("resource").forGetter(SingleSource::resource)
    ).apply(instance, SingleSource::new));

    @Override
    public SourceType<?> getType()
    {
        return SourceType.SINGLE;
    }
}
