package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector4i;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public record NineSliceTexture(boolean stretchInner, Vector4i border) implements UITexture
{
    private static final Codec<Vector4i> BORDER = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("left").forGetter(Vector4i::x),
        Codec.INT.fieldOf("top").forGetter(Vector4i::y),
        Codec.INT.fieldOf("right").forGetter(Vector4i::z),
        Codec.INT.fieldOf("bottom").forGetter(Vector4i::w)
    ).apply(instance, Vector4i::new));

    private static final Codec<Vector4i> SINGLE_OR_ALL = Codec.withAlternative(BORDER, Codec.INT, Vector4i::new);

    public static final Codec<NineSliceTexture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("stretch_inner", false).forGetter(NineSliceTexture::stretchInner),
        SINGLE_OR_ALL.fieldOf("border").forGetter(NineSliceTexture::border)
    ).apply(instance, NineSliceTexture::new));

    @Override
    public UITextureType<?> getType()
    {
        return UITextureType.NINE_SLICE;
    }
}
