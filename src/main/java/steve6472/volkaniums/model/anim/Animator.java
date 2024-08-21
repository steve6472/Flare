package steve6472.volkaniums.model.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public record Animator(AnimatorType type, List<KeyFrame> keyframes)
{
    public static final Codec<Animator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        AnimatorType.CODEC.fieldOf("type").forGetter(o -> o.type),
        KeyframeChannel.CODEC.listOf().fieldOf("keyframes").forGetter(o -> o.keyframes)
    ).apply(instance, Animator::new));
}
