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
public final class PositionKeyframe extends AnimationKeyframeChannel<Vec3DataPoint>
{
    public static final Codec<PositionKeyframe> CODEC = createKeyframe(Vec3DataPoint.CODEC, PositionKeyframe::new);

    public PositionKeyframe(Interpolation interpolation, double time, List<Vec3DataPoint> dataPoints)
    {
        super(interpolation, time, dataPoints);
    }

    @Override
    public KeyframeType<?> getType()
    {
        return KeyframeType.POSITION;
    }

    @Override
    public void processKeyframe(Vec3DataPoint first, Vec3DataPoint second, double ticks, Matrix4f transform, boolean invert, OrlangEnvironment env)
    {
        first.xyz().evaluate(env);
        second.xyz().evaluate(env);

        double x = MathUtil.lerp(first.xyz().x(), second.xyz().x(), ticks) * FlareConstants.BB_MODEL_SCALE;
        double y = MathUtil.lerp(first.xyz().y(), second.xyz().y(), ticks) * FlareConstants.BB_MODEL_SCALE;
        double z = MathUtil.lerp(first.xyz().z(), second.xyz().z(), ticks) * FlareConstants.BB_MODEL_SCALE;

        transform.translateLocal((float) -x * (invert ? -1f : 1f), (float) y * (invert ? -1f : 1f), (float) z * (invert ? -1f : 1f));
    }
}
