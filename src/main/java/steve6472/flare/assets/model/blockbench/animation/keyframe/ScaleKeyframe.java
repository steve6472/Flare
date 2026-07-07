package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.MapCodec;
import org.joml.Matrix4f;
import steve6472.core.util.MathUtil;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.Vec3DataPoint;
import steve6472.orlang.OrlangEnvironment;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class ScaleKeyframe extends AnimationKeyframeChannel<Vec3DataPoint>
{
    public static final MapCodec<ScaleKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, ScaleKeyframe::new);

    public ScaleKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)
    {
        super(KeyframeType.SCALE, interpolation, time, dataPoints);
    }

    @Override
    public void processKeyframe(Vec3DataPoint first, Vec3DataPoint second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env)
    {
        first.xyz().evaluate(env);
        second.xyz().evaluate(env);

        double x = MathUtil.lerp(first.xyz().x(), second.xyz().x(), ticks);
        double y = MathUtil.lerp(first.xyz().y(), second.xyz().y(), ticks);
        double z = MathUtil.lerp(first.xyz().z(), second.xyz().z(), ticks);

        transform.scale((float) x * (invert ? -1f : 1f), (float) y * (invert ? -1f : 1f), (float) z * (invert ? -1f : 1f));
    }

    @Override
    public MapCodec<? extends KeyFrame> codec()
    {
        return CODEC;
    }
}
