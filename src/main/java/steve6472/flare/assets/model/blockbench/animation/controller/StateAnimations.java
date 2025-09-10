package steve6472.flare.assets.model.blockbench.animation.controller;

import org.joml.Matrix4f;
import steve6472.flare.assets.model.blockbench.animation.Animation;
import steve6472.flare.assets.model.blockbench.animation.AnimationTicker;
import steve6472.flare.assets.model.blockbench.animation.Loop;
import steve6472.orlang.OrlangEnvironment;

import java.util.List;

/**
 * Created by steve6472
 * Date: 9/10/2025
 * Project: Flare <br>
 */
public class StateAnimations
{
    private AnimationTicker[] tickers;
    private Matrix4f[] transformations;

    public void start(AnimationController controller, State state)
    {
        tickers = new AnimationTicker[state.animations().size()];
        List<BlendableAnimation> animations = state.animations();
        for (int i = 0; i < animations.size(); i++)
        {
            BlendableAnimation animation = animations.get(i);
            Animation animationByName = controller.model.getAnimationByName(animation.animationName());
            tickers[i] = new AnimationTicker(animationByName, controller.model, controller.masterSkinData);
            tickers[i].timer.setLoop(animationByName.loop() == Loop.LOOP);
            tickers[i].timer.start();
        }
    }

    public void tick(Matrix4f modelTransform, OrlangEnvironment environment)
    {
        for (int i = 0; i < tickers.length; i++)
        {
            AnimationTicker ticker = tickers[i];
            ticker.tick(environment);

            if (i == 0)
            {
                transformations = ticker.skinData.toArray();
            } else
            {
                Matrix4f[] array = ticker.skinData.toArray();
                for (int j = 0; j < transformations.length; j++)
                {
                    Matrix4f transformation = transformations[j];
                    transformation.mul(array[j]);
                }
            }
        }

        for (Matrix4f transformation : transformations)
        {
            transformation.mulLocal(modelTransform);
        }
    }

    public Matrix4f[] getTransformations()
    {
        return transformations;
    }

    public Matrix4f[] getTransformations(Matrix4f[] other, float blendFactor)
    {
        for (int i = 0; i < transformations.length; i++)
        {
            transformations[i] = other[i].lerp(transformations[i], blendFactor);
        }
        return transformations;
    }
}
