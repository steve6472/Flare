package steve6472.flare.ui.textures.animation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Created by steve6472
 * Date: 12/1/2024
 * Project: Flare <br>
 */
public record SpriteAnimation(boolean interpolate, boolean useOklab, int width, int height, long frametime, List<Frame> frames)
{
    public static final Codec<SpriteAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(SpriteAnimation::interpolate),
        Codec.BOOL.optionalFieldOf("oklab", false).forGetter(SpriteAnimation::useOklab),
        Codec.INT.fieldOf("width").forGetter(SpriteAnimation::width),
        Codec.INT.fieldOf("height").forGetter(SpriteAnimation::height),
        // Time to change the frame, in milliseconds
        Codec.LONG.optionalFieldOf("frametime", 1000L).forGetter(SpriteAnimation::frametime),
        Frame.CODEC.listOf().optionalFieldOf("frames", List.of()).forGetter(SpriteAnimation::frames)
    ).apply(instance, SpriteAnimation::new));

    public record Frame(int index, Optional<Long> time)
    {
        private static final Codec<Frame> CODEC_FULL = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("index").forGetter(Frame::index),
            Codec.LONG.optionalFieldOf("time").forGetter(Frame::time)
        ).apply(instance, Frame::new));

        private static final Codec<Frame> CODEC_INDEX = Codec.INT.xmap(i -> new Frame(i, Optional.empty()), f -> f.index);

        public static final Codec<Frame> CODEC = Codec.withAlternative(CODEC_FULL, CODEC_INDEX);
    }
}
