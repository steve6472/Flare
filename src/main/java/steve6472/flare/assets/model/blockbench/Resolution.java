package steve6472.flare.assets.model.blockbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public record Resolution(int width, int height)
{
    public static final Codec<Resolution> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("width").forGetter(o -> o.width),
        Codec.INT.fieldOf("height").forGetter(o -> o.height)
    ).apply(instance, Resolution::new));
}
