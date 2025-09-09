package steve6472.flare.assets.model.blockbench.animation.keyframe;

import com.mojang.serialization.Codec;
import org.joml.Matrix4f;
import steve6472.core.util.MathUtil;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.model.blockbench.animation.Interpolation;
import steve6472.flare.assets.model.blockbench.animation.datapoint.Vec3DataPoint;
import steve6472.orlang.OrlangEnvironment;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class RotationKeyframe extends AnimationKeyframeChannel<Vec3DataPoint>
{
    public static final Codec<RotationKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, RotationKeyframe::new);

    public RotationKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    @Override
    public KeyframeType<?> getType()
    {
        return KeyframeType.ROTATION;
    }

    @Override
    public void processKeyframe(Vec3DataPoint first, Vec3DataPoint second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env)
    {
        first.xyz().evaluate(env);
        second.xyz().evaluate(env);

        double x = MathUtil.lerp(first.xyz().x(), second.xyz().x(), ticks) * FlareConstants.DEG_TO_RAD;
        double y = MathUtil.lerp(first.xyz().y(), second.xyz().y(), ticks) * FlareConstants.DEG_TO_RAD;
        double z = MathUtil.lerp(first.xyz().z(), second.xyz().z(), ticks) * FlareConstants.DEG_TO_RAD;

        transform.rotateZ((float) z * (invert ? -1f : 1f));
        transform.rotateY((float) -y * (invert ? -1f : 1f));
        transform.rotateX((float) -x * (invert ? -1f : 1f));
    }
}
