package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.module.Module;
import steve6472.core.registry.Registry;
import steve6472.core.registry.Typed;
import steve6472.flare.registry.BuiltInFlareRegistries;

import java.util.Collection;
import java.util.function.Function;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public interface Source extends Typed<Source>
{
    Codec<Source> CODEC = BuiltInFlareRegistries.ATLAS_SOURCE_TYPE.byKeyCodec().dispatch(Source::codec, Function.identity());

    static void bootstrap(Registry<MapCodec<? extends Source>> registry)
    {
        Registry.register(registry, "directory", DirectorySource.CODEC);
        Registry.register(registry, "single", SingleSource.CODEC);
        Registry.register(registry, "filter", FilterSource.CODEC);
    }

    Collection<SourceResult> load(Module module, String namespace);
}
