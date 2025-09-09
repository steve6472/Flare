package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import steve6472.core.registry.Typed;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public interface KeyFrame extends Typed<KeyframeType<?>>
{
    Codec<KeyFrame> CODEC = FlareRegistries.KEYFRAME_TYPE.byKeyCodec().dispatch("channel", KeyFrame::getType, KeyframeType::mapCodec);

    double time();
    Interpolation interpolation();
}