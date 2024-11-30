package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Key;
import steve6472.core.registry.Type;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class UITextureType<T extends UITexture> extends Type<T>
{
    public static final UITextureType<StretchTexture> STRETCH = register("stretch", StretchTexture.CODEC);
    public static final UITextureType<NineSliceTexture> NINE_SLICE = register("nine_slice", NineSliceTexture.CODEC);
    public static final UITextureType<TileTexture> TILE = register("tile", TileTexture.CODEC);

    public UITextureType(Key key, MapCodec<T> codec)
    {
        super(key, codec);
    }

    private static <T extends UITexture> UITextureType<T> register(String id, Codec<T> codec)
    {
        var obj = new UITextureType<>(Key.defaultNamespace(id), MapCodec.assumeMapUnsafe(codec));
        FlareRegistries.UI_TEXTURE_TYPE.register(obj);
        return obj;
    }
}
