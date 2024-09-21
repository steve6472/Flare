package steve6472.volkaniums.assets.model.blockbench.outliner;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3f;
import steve6472.volkaniums.Constants;
import steve6472.volkaniums.util.ExtraCodecs;

import java.util.List;
import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public final class OutlinerElement extends OutlinerUUID
{
    private static final Codec<OutlinerElement> RECURSIVE_CODEC = Codec.recursive("Outliner",
        selfCodec ->
            RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.UUID.fieldOf("uuid").forGetter(o -> o.uuid),
                ExtraCodecs.VEC_3F.optionalFieldOf("origin", new Vector3f()).forGetter(o -> o.origin),
                ExtraCodecs.VEC_3F.optionalFieldOf("rotation", new Vector3f()).forGetter(o -> o.rotation),
                Codec.withAlternative(OutlinerUUID.CODEC, selfCodec).listOf().optionalFieldOf("children", List.of()).forGetter(o -> o.children)
            ).apply(instance, (uuid1, origin1, rotation1, children1) -> new OutlinerElement(uuid1, origin1.mul(Constants.BB_MODEL_SCALE), rotation1.mul(Constants.DEG_TO_RAD), children1)))
        );

    public static final Codec<OutlinerUUID> CODEC = Codec.withAlternative(OutlinerUUID.CODEC, RECURSIVE_CODEC);

    private final Vector3f origin;
    private final Vector3f rotation;
    private final List<OutlinerUUID> children;

    public OutlinerElement(UUID uuid, Vector3f origin, Vector3f rotation, List<OutlinerUUID> children)
    {
        super(uuid);
        this.origin = origin;
        this.rotation = rotation;
        this.children = children;
        fixParents();
    }

    public Vector3f origin()
    {
        return origin;
    }

    public Vector3f rotation()
    {
        return rotation;
    }

    public List<OutlinerUUID> children()
    {
        return children;
    }

    private void fixParents()
    {
        for (OutlinerUUID child : children)
        {
            child.parent = this;
        }
    }

    @Override
    public String toString()
    {
        return "OutlinerElement{" + "origin=" + origin + ", rotation=" + rotation + ", children=" + children + ", uuid=" + uuid + '}';
    }
}
