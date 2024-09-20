package steve6472.volkaniums.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import steve6472.volkaniums.Main;
import steve6472.volkaniums.util.Preconditions;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by steve6472
 * Date: 5/4/2024
 * Project: Domin <br>
 */
public class Key
{
    public static final Codec<Key> CODEC = Codec.STRING.flatXmap(string -> {
        if (!string.contains(":"))
        {
            return DataResult.success(defaultNamespace(string));
        } else
        {
            String[] split = string.split(":");
            return DataResult.success(withNamespace(split[0], split[1]));
        }
    }, id -> DataResult.success(id.toString()));

    private static final Pattern ID_MATCH = Pattern.compile("[a-z0-9_/]*");

    private final String namespace;
    private final String id;

    private Key(String namespace, String id)
    {
        Preconditions.matchPattern(ID_MATCH, namespace, () -> "Namespace contains illegal characters, allowed: [a-z0-9_/]*");
        Preconditions.matchPattern(ID_MATCH, id, () -> "ID contains illegal characters, allowed: [a-z0-9_/]*");

        this.namespace = namespace;
        this.id = id;
    }

    public static Key withNamespace(String namespace, String id)
    {
        return new Key(namespace, id);
    }

    public static Key defaultNamespace(String id)
    {
        return new Key(Main.BASE_NAMESPACE, id);
    }

    public String namespace()
    {
        return namespace;
    }

    public String id()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return namespace + ":" + id;
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        Key key = (Key) object;
        return Objects.equals(namespace, key.namespace) && Objects.equals(id, key.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(namespace, id);
    }
}
