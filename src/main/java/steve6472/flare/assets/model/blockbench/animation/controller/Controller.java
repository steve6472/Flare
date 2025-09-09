package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public record Controller(String initialState, Map<String, State> states)
{
    public static final Codec<Controller> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("initial_state").forGetter(Controller::initialState),
        Codec.unboundedMap(Codec.STRING, State.CODEC).fieldOf("states").forGetter(Controller::states)
    ).apply(instance, Controller::new));

    public Controller copy()
    {
        Map<String, State> statesCopy = new HashMap<>(states.size());
        states.forEach((k, v) -> statesCopy.put(k, v.copy()));
        return new Controller(initialState, Map.copyOf(statesCopy));
    }
}
