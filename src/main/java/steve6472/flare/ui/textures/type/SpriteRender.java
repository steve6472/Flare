package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import steve6472.core.registry.Typed;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public interface SpriteRender extends Typed<SpriteRenderType<?>>
{
    Codec<SpriteRender> CODEC = FlareRegistries.SPRITE_RENDER_TYPE.byKeyCodec().dispatch("type", SpriteRender::getType, SpriteRenderType::mapCodec);
}
