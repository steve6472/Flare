package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector4i;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public record NineSlice(Stretch stretch, Vector4i border) implements SpriteRender
{
    private static final Codec<Vector4i> BORDER = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("left").forGetter(Vector4i::x),
        Codec.INT.fieldOf("top").forGetter(Vector4i::y),
        Codec.INT.fieldOf("right").forGetter(Vector4i::z),
        Codec.INT.fieldOf("bottom").forGetter(Vector4i::w)
    ).apply(instance, Vector4i::new));

    private static final Codec<Vector4i> SINGLE_OR_ALL = Codec.withAlternative(BORDER, Codec.INT, Vector4i::new);

    public static final Codec<NineSlice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Stretch.CODEC.optionalFieldOf("stretch", Stretch.DEFAULT).forGetter(NineSlice::stretch),
        SINGLE_OR_ALL.fieldOf("border").forGetter(NineSlice::border)
    ).apply(instance, NineSlice::new));

    @Override
    public SpriteRenderType<?> getType()
    {
        return SpriteRenderType.NINE_SLICE;
    }

    public record Stretch(boolean inner, boolean left, boolean right, boolean top, boolean bottom)
    {
        public static final Codec<Stretch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("inner", false).forGetter(Stretch::inner),
            Codec.BOOL.optionalFieldOf("left", false).forGetter(Stretch::left),
            Codec.BOOL.optionalFieldOf("right", false).forGetter(Stretch::right),
            Codec.BOOL.optionalFieldOf("top", false).forGetter(Stretch::top),
            Codec.BOOL.optionalFieldOf("bottom", false).forGetter(Stretch::bottom)
        ).apply(instance, Stretch::new));

        public static final Stretch DEFAULT = new Stretch(false, false, false, false, false);
    }
}
