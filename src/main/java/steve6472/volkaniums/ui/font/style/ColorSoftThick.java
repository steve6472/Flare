package steve6472.volkaniums.ui.font.style;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector4f;
import steve6472.core.util.ExtraCodecs;

/**
 * Created by steve6472
 * Date: 11/10/2024
 * Project: Volkaniums <br>
 */
public record ColorSoftThick(Vector4f color, float softness, float thickness)
{
    public static final Codec<ColorSoftThick> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.VEC_4F.optionalFieldOf("color", new Vector4f(1, 1, 1, 1)).forGetter(ColorSoftThick::color),
        Codec.FLOAT.optionalFieldOf("softness", 0f).forGetter(ColorSoftThick::softness),
        Codec.FLOAT.optionalFieldOf("thickness", 0f).forGetter(ColorSoftThick::thickness)
    ).apply(instance, ColorSoftThick::new));

    public static final ColorSoftThick EMPTY = new ColorSoftThick(new Vector4f(0, 0, 0, 0), 0, 0);
}
