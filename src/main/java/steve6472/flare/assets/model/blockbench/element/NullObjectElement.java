package steve6472.flare.assets.model.blockbench.element;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.core.util.ExtraCodecs;
import steve6472.core.util.ImagePacker;
import steve6472.flare.Constants;
import steve6472.flare.assets.model.blockbench.Element;
import steve6472.flare.assets.model.blockbench.ElementType;
import steve6472.flare.assets.model.blockbench.LoadedModel;

import java.util.List;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public record NullObjectElement(UUID uuid, String name, Vector3f position, String ikTarget, String ikSource, boolean lockIkTargetRotation) implements Element
{
    public static final Codec<NullObjectElement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
        Codec.STRING.fieldOf("name").forGetter(o -> o.name),
        ExtraCodecs.VEC_3F.fieldOf("position").forGetter(o -> o.position),
        Codec.STRING.fieldOf("ik_target").forGetter(o -> o.ikTarget),
        Codec.STRING.fieldOf("ik_source").forGetter(o -> o.ikTarget),
        Codec.BOOL.fieldOf("lock_ik_target_rotation").forGetter(o -> o.lockIkTargetRotation)
        ).apply(instance, (uuid1, name1, position1, ikTarget1, ikSource1, lockIkTargetRotation1) ->
        new NullObjectElement(
            uuid1,
            name1,
            position1.mul(Constants.BB_MODEL_SCALE),
            ikTarget1,
            ikSource1,
            lockIkTargetRotation1))
    );

    @Override
    public ElementType<?> getType()
    {
        return ElementType.NULL_OBJECT;
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
