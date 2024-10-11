package steve6472.volkaniums.assets.model.blockbench.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.core.util.ExtraCodecs;
import steve6472.core.util.ImagePacker;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.assets.model.blockbench.Element;
import steve6472.volkaniums.assets.model.blockbench.ElementType;
import steve6472.volkaniums.assets.model.blockbench.LoadedModel;

import java.util.*;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public record LocatorElement(UUID uuid, String name, Vector3f position, Vector3f rotation, boolean ignoreInheretedScale) implements Element
{
    public static final Codec<LocatorElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        Codec.STRING.fieldOf("name").forGetter(o -> o.name),
        ExtraCodecs.VEC_3F.fieldOf("position").forGetter(o -> o.position),
        ExtraCodecs.VEC_3F.fieldOf("rotation").forGetter(o -> o.rotation),
        Codec.BOOL.fieldOf("ignore_inherited_scale").forGetter(o -> o.ignoreInheretedScale)
        ).apply(instance, (uuid1, name1, position1, rotation1, ignoreInheretedScale1) ->
        new LocatorElement(
            uuid1,
            name1,
            position1.mul(Constants.BB_MODEL_SCALE),
            rotation1.mul(Constants.DEG_TO_RAD),
            ignoreInheretedScale1))
    );

    @Override
    public ElementType<?> getType()
    {
        return ElementType.LOCATOR;
    }

    @Override
    public void fixUvs(LoadedModel model, ImagePacker packer) { }

    @Override
    public List<Vector3f> toVertices()
    {
        return List.of();
    }

    @Override
    public List<Vector3f> toNormals()
    {
        return List.of();
    }

    @Override
    public List<Vector2f> toUVs()
    {
        return List.of();
    }
}
