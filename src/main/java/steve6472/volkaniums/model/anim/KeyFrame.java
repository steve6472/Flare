package steve6472.volkaniums.model.anim;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.registry.Typed;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public interface KeyFrame extends Typed<KeyframeType<?>>
{
    Codec<KeyFrame> CODEC = Registries.KEYFRAME_TYPE.byKeyCodec().dispatch("channel", KeyFrame::getType, KeyframeType::mapCodec);
}