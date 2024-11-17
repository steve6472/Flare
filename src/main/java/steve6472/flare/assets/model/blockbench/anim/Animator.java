package steve6472.flare.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public record Animator(AnimatorType type, boolean rotationGlobal, List<KeyFrame> keyframes)
{
    public static final Codec<Animator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        AnimatorType.CODEC.fieldOf("type").forGetter(o -> o.type),
        Codec.BOOL.optionalFieldOf("rotation_global", true).forGetter(o -> o.rotationGlobal),
        KeyframeChannel.CODEC.listOf().fieldOf("keyframes").forGetter(o -> o.keyframes)
    ).apply(instance, Animator::new));
}
