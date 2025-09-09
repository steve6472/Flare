package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Key;
import steve6472.core.registry.Type;
import steve6472.flare.FlareConstants;
import steve6472.flare.registry.FlareRegistries;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public final class KeyframeType<T extends KeyFrame> extends Type<T>
{
    public static final KeyframeType<RotationKeyframe> ROTATION = register("rotation", RotationKeyframe.CODEC);
    public static final KeyframeType<PositionKeyframe> POSITION = register("position", PositionKeyframe.CODEC);
    public static final KeyframeType<ScaleKeyframe> SCALE = register("scale", ScaleKeyframe.CODEC);

    public static final KeyframeType<ParticleKeyframe> PARTICLE = register("particle", ParticleKeyframe.CODEC);
    public static final KeyframeType<SoundKeyframe> SOUND = register("sound", SoundKeyframe.CODEC);
    public static final KeyframeType<TimelineKeyframe> TIMELINE = register("timeline", TimelineKeyframe.CODEC);

    public KeyframeType(Key key, MapCodec<T> codec)
    {
        super(key, codec);
    }

    private static <T extends KeyFrame> KeyframeType<T> register(String id, Codec<T> codec)
    {
        var obj = new KeyframeType<>(FlareConstants.key(id), MapCodec.assumeMapUnsafe(codec));

        FlareRegistries.KEYFRAME_TYPE.register(obj);
        return obj;
    }

    @Override
    public String toString()
    {
        return "KeyframeType{key='" + key() + "'}";
    }
}
