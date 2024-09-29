package steve6472.volkaniums.assets.model.blockbench.anim;

import com.mojang.serialization.Codec;
import steve6472.core.registry.Typed;
import steve6472.volkaniums.Registries;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Volkaniums <br>
 */
public interface KeyFrame extends Typed<KeyframeType<?>>
{
    Codec<KeyFrame> CODEC = Registries.KEYFRAME_TYPE.byKeyCodec().dispatch("channel", KeyFrame::getType, KeyframeType::mapCodec);

    double time();
    Interpolation interpolation();
}