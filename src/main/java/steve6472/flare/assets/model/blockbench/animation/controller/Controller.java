package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4f;

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
    private StateAnimations previousAnimations;
    private StateAnimations currentAnimations;
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
        if (currentAnimations == null)
        {
            currentState = states.get(initialState);
            currentAnimations = new StateAnimations();
            currentAnimations.start(controller, currentState);
        }

        Optional<String> nextStateOpt = currentState.getNextState(controller.environment());
        nextStateOpt.ifPresent(nextStateName ->
        {
            State nextState = states.get(nextStateName);
            beginBlendTransition(currentState.blendTransitionSeconds());
            previousAnimations = currentAnimations;
            currentAnimations = new StateAnimations();
            currentAnimations.start(controller, nextState);
            currentState = nextState;
        });

        if (previousAnimations != null)
            previousAnimations.tick(modelTransform, controller.environment());

        currentAnimations.tick(modelTransform, controller.environment());

        float blendFactor = calculateCurrentBlendFactor();

        if (blendFactor >= blendTime && previousAnimations != null)
        {
            finishBlendTransition();
            blendFactor = 0f;
        }

        if (blendFactor == 0f || previousAnimations == null)
        {
            controller.transformations = currentAnimations.getTransformations();
        } else
        {
            controller.transformations = currentAnimations.getTransformations(previousAnimations.getTransformations(), blendFactor * blendMult);
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
        previousAnimations = null;
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
