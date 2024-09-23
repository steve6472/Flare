package steve6472.volkaniums.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.volkaniums.util.ExtraCodecs;

import java.util.Map;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public record Animation(UUID uuid, String name, Loop loop, double length, Map<String, Animator> animators)
{
    public static final Codec<Animation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        Codec.STRING.fieldOf("name").forGetter(o -> o.name),
        Loop.CODEC.fieldOf("loop").forGetter(o -> o.loop),
        Codec.DOUBLE.fieldOf("length").forGetter(o -> o.length),
        ExtraCodecs.mapListCodec(Codec.STRING, Animator.CODEC).optionalFieldOf("animators", Map.of()).forGetter(o -> o.animators)
    ).apply(instance, Animation::new));
}
