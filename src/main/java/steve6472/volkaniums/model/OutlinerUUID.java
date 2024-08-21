package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import steve6472.volkaniums.util.ExtraCodecs;

import java.util.UUID;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public class OutlinerUUID
{
    public static final Codec<OutlinerUUID> CODEC = ExtraCodecs.UUID.xmap(OutlinerUUID::new, OutlinerUUID::uuid);

    protected final UUID uuid;

    public OutlinerUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    public UUID uuid()
    {
        return uuid;
    }

    @Override
    public String toString()
    {
        return "OutlinerUUID{" + "uuid=" + uuid + '}';
    }
}
