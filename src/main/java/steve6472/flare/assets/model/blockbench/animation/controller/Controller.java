package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4f;
import steve6472.flare.assets.model.blockbench.animation.Animation;
import steve6472.flare.assets.model.blockbench.animation.AnimationTicker;
import steve6472.flare.assets.model.blockbench.animation.Loop;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public final class Controller
{
    public static final Codec<Controller> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("initial_state").forGetter(Controller::initialState),
        Codec.unboundedMap(Codec.STRING, State.CODEC).fieldOf("states").forGetter(Controller::states)
    ).apply(instance, Controller::new));

    private final String initialState;
    private final Map<String, State> states;

    private AnimationController controller;
    private AnimationTicker previousAnimation;
    private AnimationTicker currentAnimation;
    private State currentState;
    private float blendTime;
    private float blendMult;
    private long blendTimeStart;

    public Controller(String initialState, Map<String, State> states)
    {
        this.initialState = initialState;
        this.states = states;
    }

    public void tick(Matrix4f modelTransform)
    {
        if (currentAnimation == null)
        {
            currentState = states.get(initialState);
            String animationName = currentState.animations().getFirst();
            Animation animationByName = controller.model.getAnimationByName(animationName);
            currentAnimation = new AnimationTicker(animationByName, controller.model, controller.masterSkinData);
            currentAnimation.timer.start();
            currentAnimation.timer.setLoop(animationByName.loop() == Loop.LOOP);
        }

        Optional<String> nextStateOpt = currentState.getNextState(controller.environment());
        nextStateOpt.ifPresent(nextStateName ->
        {
            State nextState = states.get(nextStateName);
            String nextAnimationName = nextState.animations().getFirst();
            Animation nextAnimationByName = controller.model.getAnimationByName(nextAnimationName);
            beginBlendTransition(currentState.blendTransitionSeconds());
            previousAnimation = currentAnimation;
            currentAnimation = new AnimationTicker(nextAnimationByName, controller.model, controller.masterSkinData);
            currentAnimation.timer.start();
            currentAnimation.timer.setLoop(nextAnimationByName.loop() == Loop.LOOP);
            currentState = nextState;
        });

        if (previousAnimation != null)
            previousAnimation.tick(modelTransform, controller.environment());

        currentAnimation.tick(modelTransform, controller.environment());

        float blendFactor = calculateCurrentBlendFactor();

        if (blendFactor >= blendTime && previousAnimation != null)
        {
            finishBlendTransition();
            blendFactor = 0f;
        }

        if (blendFactor == 0f || previousAnimation == null)
        {
            controller.transformations = currentAnimation.skinData.toArray();
        } else
        {
            controller.transformations = previousAnimation.skinData.toArray(currentAnimation.skinData, blendFactor * blendMult);
        }
    }

    private void beginBlendTransition(float blendTarget)
    {
        blendTimeStart = System.nanoTime();
        blendTime = blendTarget;
        blendMult = 1f / blendTarget;
    }

    private float calculateCurrentBlendFactor()
    {
        long now = System.nanoTime();
        long elapsedNanos = now - blendTimeStart;
        return elapsedNanos / 1e9f;
    }

    private void finishBlendTransition()
    {
        blendTime = 0;
        blendTimeStart = 0;
        previousAnimation = null;
    }

    public String initialState()
    {
        return initialState;
    }

    public Map<String, State> states()
    {
        return states;
    }

    public Controller copy(AnimationController controller)
    {
        Map<String, State> statesCopy = new HashMap<>(states.size());
        states.forEach((k, v) -> statesCopy.put(k, v.copy()));
        Controller result = new Controller(initialState, Map.copyOf(statesCopy));
        result.controller = controller;
        return result;
    }

    @Override
    public String toString()
    {
        return "Controller[" + "initialState=" + initialState + ", " + "states=" + states + ']';
    }
}
