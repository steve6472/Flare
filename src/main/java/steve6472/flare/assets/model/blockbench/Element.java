package steve6472.flare.assets.model.blockbench;

import com.mojang.serialization.Codec;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.core.registry.Typed;
import steve6472.core.util.ImagePacker;
import steve6472.flare.registry.FlareRegistries;

import java.util.List;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public interface Element extends Typed<ElementType<?>>
{
    Codec<Element> CODEC = FlareRegistries.MODEL_ELEMENT.byKeyCodec().dispatch("type", Element::getType, ElementType::mapCodec);

    UUID uuid();
    String name();

    void fixUvs(LoadedModel model, ImagePacker packer);

    List<Vector3f> toVertices();
    List<Vector3f> toNormals();
    List<Vector2f> toUVs();
}
