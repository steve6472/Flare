package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Matrix4f;
import steve6472.orlang.OrlangEnvironment;

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

    AnimationController controller;
    private StateAnimations previousAnimations;
    private StateAnimations currentAnimations;
    private State currentState;
    private float blendTime;
    private float blendMult;
    private long blendTimeStart;
    Matrix4f[] transformations;

    public Controller(String initialState, Map<String, State> states)
    {
        this.initialState = initialState;
        this.states = states;
    }

    public void tick(OrlangEnvironment environment)
    {
        if (currentAnimations == null)
        {
            currentState = states.get(initialState);
            currentAnimations = new StateAnimations();
            currentAnimations.start(controller, currentState);
        }

        currentAnimations.updateAnimationQueries(environment);

        Optional<String> nextStateOpt = currentState.getNextState(environment);
        nextStateOpt.ifPresent(this::forceTransition);

        if (previousAnimations != null)
            previousAnimations.tick(environment, this);

        currentAnimations.tick(environment, this);

        float blendFactor = calculateCurrentBlendFactor();

        if (blendFactor >= blendTime && previousAnimations != null)
        {
            finishBlendTransition();
            blendFactor = 0f;
        }

        if (blendFactor == 0f || previousAnimations == null)
        {
            transformations = currentAnimations.transformations;
        } else
        {
            transformations = currentAnimations.getTransformations(previousAnimations.transformations, blendFactor * blendMult);
        }
    }

    public void forceTransition(String state)
    {
        State nextState = states.get(state);
        beginBlendTransition(currentState.blendTransitionSeconds());
        previousAnimations = currentAnimations;
        currentAnimations = new StateAnimations();
        currentAnimations.start(controller, nextState);
        currentState = nextState;
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

    public Matrix4f[] copyTransformations()
    {
        Matrix4f[] copy = new Matrix4f[transformations.length];
        for (int i = 0; i < transformations.length; i++)
        {
            copy[i] = new Matrix4f(transformations[i]);
        }
        return copy;
    }

    @Override
    public String toString()
    {
        return "Controller[" + "initialState=" + initialState + ", " + "states=" + states + ']';
    }
}
