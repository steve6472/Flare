package steve6472.flare.assets.atlas.source;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Key;
import steve6472.core.registry.Type;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 7/16/2025
 * Project: Flare <br>
 */
public final class SourceType<T extends Source> extends Type<T>
{
    public static final SourceType<DirectorySource> DIRECTORY = register("directory", DirectorySource.CODEC);
    public static final SourceType<SingleSource> SINGLE = register("single", SingleSource.CODEC);

    public SourceType(Key key, MapCodec<T> codec)
    {
        super(key, codec);
    }

    private static <T extends Source> SourceType<T> register(String id, Codec<T> codec)
    {
        var obj = new SourceType<>(FlareConstants.key(id), MapCodec.assumeMapUnsafe(codec));
        FlareRegistries.ATLAS_SOURCE_TYPE.register(obj);
        return obj;
    }
}
