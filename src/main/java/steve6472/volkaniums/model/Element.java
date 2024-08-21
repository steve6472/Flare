package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.registry.Typed;

import java.util.List;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public interface Element extends Typed<ElementType<?>>
{
    Codec<Element> CODEC = Registries.MODEL_ELEMENT.byKeyCodec().dispatch("type", Element::getType, ElementType::mapCodec);

    UUID uuid();

    List<Vector3f> toVertices();

    List<Vector2f> toUVs();
}
