package steve6472.flare.ui.textures.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Created by steve6472
 * Date: 12/1/2024
 * Project: Flare <br>
 */
public record SpriteAnimation(boolean interpolate, int width, int height, int frametime)
{
    public static final Codec<SpriteAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(SpriteAnimation::interpolate),
        Codec.INT.fieldOf("width").forGetter(SpriteAnimation::width),
        Codec.INT.fieldOf("height").forGetter(SpriteAnimation::height),
        // Time to change the frame, in game ticks
        Codec.INT.optionalFieldOf("frametime", 1).forGetter(SpriteAnimation::frametime)
    ).apply(instance, SpriteAnimation::new));
}
