package steve6472.volkaniums.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.registry.Key;
import steve6472.volkaniums.registry.Type;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public final class ElementType<T extends Element> extends Type<T>
{
    public static final ElementType<CubeElement> CUBE = register("cube", CubeElement.CODEC);
    public static final ElementType<MeshElement> MESH = register("mesh", MeshElement.CODEC);

    public ElementType(Key key, MapCodec<T> codec)
    {
        super(key, codec);
    }

    private static <T extends Element> ElementType<T> register(String id, Codec<T> codec)
    {
        var obj = new ElementType<>(Key.defaultNamespace(id), MapCodec.assumeMapUnsafe(codec));
        Registries.MODEL_ELEMENT.register(obj);
        return obj;
    }
}
