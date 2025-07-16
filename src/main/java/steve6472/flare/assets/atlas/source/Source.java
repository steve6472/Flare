package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import steve6472.core.registry.Typed;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public interface Source extends Typed<SourceType<?>>
{
    Codec<Source> CODEC = FlareRegistries.ATLAS_SOURCE_TYPE.byKeyCodec().dispatch("type", Source::getType, SourceType::mapCodec);
}
