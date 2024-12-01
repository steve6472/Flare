package steve6472.flare.ui.textures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.flare.ui.textures.animation.SpriteAnimation;
import steve6472.flare.ui.textures.type.SpriteRender;
import steve6472.flare.ui.textures.type.Stretch;

import java.util.Optional;

/**
 * Created by steve6472
 * Date: 12/1/2024
 * Project: Flare <br>
 */
public record SpriteData(SpriteRender renderType, Optional<SpriteAnimation> animation)
{
    public static final Codec<SpriteData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SpriteRender.CODEC.optionalFieldOf("render", Stretch.instance()).forGetter(SpriteData::renderType),
        SpriteAnimation.CODEC.optionalFieldOf("animation").forGetter(SpriteData::animation)
    ).apply(instance, SpriteData::new));

    public static final SpriteData DEFAULT = new SpriteData(Stretch.instance(), Optional.empty());
}
