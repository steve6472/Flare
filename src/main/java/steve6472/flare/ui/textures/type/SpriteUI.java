package steve6472.flare.ui.textures.type;

import com.mojang.serialization.Codec;
import steve6472.core.registry.Typed;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 11/27/2024
 * Project: Flare <br>
 */
public interface SpriteUI extends Typed<SpriteUIType<?>>
{
    Codec<SpriteUI> CODEC = FlareRegistries.SPRITE_UI_TYPE.byKeyCodec().dispatch("type", SpriteUI::getType, SpriteUIType::mapCodec);
}
