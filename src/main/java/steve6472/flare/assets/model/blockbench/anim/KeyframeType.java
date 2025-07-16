package steve6472.flare.assets.model.blockbench.anim;

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
    public static final KeyframeType<KeyframeChannel.RotationKeyframe> ROTATION = register("rotation", KeyframeChannel.RotationKeyframe.CODEC);
    public static final KeyframeType<KeyframeChannel.PositionKeyframe> POSITION = register("position", KeyframeChannel.PositionKeyframe.CODEC);
    public static final KeyframeType<KeyframeChannel.ScaleKeyframe> SCALE = register("scale", KeyframeChannel.ScaleKeyframe.CODEC);

    public static final KeyframeType<KeyframeChannel.ParticleKeyframe> PARTICLE = register("particle", KeyframeChannel.ParticleKeyframe.CODEC);
    public static final KeyframeType<KeyframeChannel.SoundKeyframe> SOUND = register("sound", KeyframeChannel.SoundKeyframe.CODEC);
    public static final KeyframeType<KeyframeChannel.TimelineKeyframe> TIMELINE = register("timeline", KeyframeChannel.TimelineKeyframe.CODEC);

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
