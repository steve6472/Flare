package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector4i;
import steve6472.core.registry.Key;
import steve6472.core.util.ExtraCodecs;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public final class StretchTexture implements UITexture
{
    private static final StretchTexture INSTANCE = new StretchTexture();
    public static final Codec<StretchTexture> CODEC = Codec.unit(INSTANCE);

    private StretchTexture()
    {
    }

    @Override
    public UITextureType<?> getType()
    {
        return UITextureType.STRETCH;
    }

    public static StretchTexture instance()
    {
        return INSTANCE;
    }
}
