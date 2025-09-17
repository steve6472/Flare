package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import steve6472.orlang.OrlangEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public record State(List<BlendableAnimation> animations, List<Transition> transitions, float blendTransitionSeconds)
{
    public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlendableAnimation.CODEC.listOf().optionalFieldOf("animations", List.of()).forGetter(State::animations),
        Transition.CODEC.listOf().optionalFieldOf("transitions", List.of()).forGetter(State::transitions),
        Codec.FLOAT.optionalFieldOf("blend_transition", 0f).forGetter(State::blendTransitionSeconds)
    ).apply(instance, State::new));

    public Optional<String> getNextState(OrlangEnvironment environment)
    {
        for (Transition transition : transitions)
        {
            if (transition.shouldTransition(environment))
                return Optional.of(transition.state());
        }
        return Optional.empty();
    }

    public State copy()
    {
        List<Transition> transitionsCopy = transitions.stream().map(Transition::copy).collect(Collectors.toCollection(() -> new ArrayList<>(transitions.size())));
        return new State(List.copyOf(animations), List.copyOf(transitionsCopy), blendTransitionSeconds);
    }
}
