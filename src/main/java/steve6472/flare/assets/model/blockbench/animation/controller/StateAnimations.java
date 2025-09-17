package steve6472.flare.assets.model.blockbench.animation.controller;

import org.joml.Matrix4f;
import steve6472.flare.assets.model.blockbench.animation.Animation;
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
    Matrix4f[] transformations;

    public void start(AnimationController controller, State state)
    {
        tickers = new AnimationTicker[state.animations().size()];
        List<BlendableAnimation> animations = state.animations();
        for (int i = 0; i < animations.size(); i++)
        {
            BlendableAnimation animation = animations.get(i);
            Animation animationByName = controller.model.getAnimationByName(animation.animationName());
            tickers[i] = new AnimationTicker(animationByName, controller);
            tickers[i].timer.setLoop(animationByName.loop() == Loop.LOOP);
            tickers[i].timer.start();
        }
    }

    public void updateAnimationQueries(OrlangEnvironment environment)
    {
        boolean anyEnded = tickers.length == 0;
        boolean allEnded = true;

        for (AnimationTicker ticker : tickers)
        {
            if (ticker.timer.hasEnded())
            {
                anyEnded = true;
            } else
            {
                allEnded = false;
            }
        }

        if (environment.queryFunctionSet instanceof AnimationQuery animationQuery)
        {
            animationQuery.setAnyAnimationFinished(anyEnded);
            animationQuery.setAllAnimationsFinished(allEnded);
        }
    }

    public void tick(OrlangEnvironment environment, Controller controller)
    {
        if (tickers.length == 0)
        {
            transformations = controller.controller.masterSkinData.toArrayCopy();
        }

        for (int i = 0; i < tickers.length; i++)
        {
            AnimationTicker ticker = tickers[i];
            ticker.tick(environment, controller);

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
