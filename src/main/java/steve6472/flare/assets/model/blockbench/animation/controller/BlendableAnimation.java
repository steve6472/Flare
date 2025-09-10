package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.orlang.codec.OrNumValue;

/**
 * Created by steve6472
 * Date: 9/10/2025
 * Project: Flare <br>
 */
public record BlendableAnimation(String animationName, OrNumValue blendFactor)
{
    private static final Codec<BlendableAnimation> CODEC_STRING = Codec.STRING.xmap(s -> new BlendableAnimation(s, new OrNumValue(1.0)), a -> a.animationName);
    private static final Codec<BlendableAnimation> CODEC_BLEND = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("animation").forGetter(BlendableAnimation::animationName),
        OrNumValue.CODEC.fieldOf("blend_factor").forGetter(BlendableAnimation::blendFactor)
    ).apply(instance, BlendableAnimation::new));

    public static final Codec<BlendableAnimation> CODEC = Codec.withAlternative(CODEC_BLEND, CODEC_STRING).validate(o -> {
        if (!o.blendFactor.isConstant() && o.blendFactor.get() != 1.0)
            return DataResult.error(() -> "Animation blend is currently not supported");
        return DataResult.success(o);
    });
}
