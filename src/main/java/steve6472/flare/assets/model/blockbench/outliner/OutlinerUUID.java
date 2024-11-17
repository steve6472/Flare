package steve6472.flare.assets.model.blockbench.outliner;

import com.mojang.serialization.Codec;
import steve6472.core.util.ExtraCodecs;

import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public class OutlinerUUID
{
    public static final Codec<OutlinerUUID> CODEC = ExtraCodecs.UUID.xmap(OutlinerUUID::new, OutlinerUUID::uuid);

    protected final UUID uuid;
    OutlinerUUID parent;

    public OutlinerUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    public UUID uuid()
    {
        return uuid;
    }

    public OutlinerUUID parent()
    {
        return parent;
    }

    @Override
    public String toString()
    {
        return "OutlinerUUID{" + "uuid=" + uuid + '}';
    }
}
