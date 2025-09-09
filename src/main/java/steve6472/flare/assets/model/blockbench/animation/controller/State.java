package steve6472.flare.assets.model.blockbench.animation.controller;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by steve6472
 * Date: 9/8/2025
 * Project: Flare <br>
 */
public record State(List<String> animations, List<Transition> transitions, double blendTransitionSeconds)
{
    public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().fieldOf("animations").forGetter(State::animations),
        Transition.CODEC.listOf().fieldOf("transitions").forGetter(State::transitions),
        Codec.DOUBLE.optionalFieldOf("blend_transition", 0.2d).forGetter(State::blendTransitionSeconds)
    ).apply(instance, State::new));

    public State copy()
    {
        List<Transition> transitionsCopy = transitions.stream().map(Transition::copy).collect(Collectors.toCollection(() -> new ArrayList<>(transitions.size())));
        return new State(List.copyOf(animations), List.copyOf(transitionsCopy), blendTransitionSeconds);
    }
}
