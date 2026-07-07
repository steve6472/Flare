package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Registry;
import steve6472.core.registry.Typed;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.registry.BuiltInFlareRegistries;

import java.util.function.Function;

/**
 * Created by steve6472
 * Date: 8/20/2024
 * Project: Flare <br>
 */
public interface KeyFrame extends Typed<KeyFrame>
{
    Codec<KeyFrame> CODEC = BuiltInFlareRegistries.KEYFRAME_TYPE.byKeyCodec().dispatch("channel", KeyFrame::codec, Function.identity());

    static void bootstrap(Registry<MapCodec<? extends KeyFrame>> registry)
    {
        Registry.register(registry, "rotation", RotationKeyframe.CODEC);
        Registry.register(registry, "position", PositionKeyframe.CODEC);
        Registry.register(registry, "scale", ScaleKeyframe.CODEC);

        Registry.register(registry, "particle", ParticleKeyframe.CODEC);
        Registry.register(registry, "sound", SoundKeyframe.CODEC);
        Registry.register(registry, "timeline", TimelineKeyframe.CODEC);
    }

    double time();
    Interpolation interpolation();
    KeyframeType type();
}