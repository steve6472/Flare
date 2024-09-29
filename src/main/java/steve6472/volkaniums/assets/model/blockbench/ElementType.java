package steve6472.volkaniums.assets.model.blockbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import steve6472.core.registry.Key;
import steve6472.core.registry.Type;
import steve6472.volkaniums.Registries;
import steve6472.volkaniums.assets.model.blockbench.element.CubeElement;
import steve6472.volkaniums.assets.model.blockbench.element.LocatorElement;
import steve6472.volkaniums.assets.model.blockbench.element.MeshElement;
import steve6472.volkaniums.assets.model.blockbench.element.NullObjectElement;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Volkaniums <br>
 */
public final class ElementType<T extends Element> extends Type<T>
{
    public static final ElementType<CubeElement> CUBE = register("cube", CubeElement.CODEC);
    public static final ElementType<MeshElement> MESH = register("mesh", MeshElement.CODEC);
    public static final ElementType<LocatorElement> LOCATOR = register("locator", LocatorElement.CODEC);
    public static final ElementType<NullObjectElement> NULL_OBJECT = register("null_object", NullObjectElement.CODEC);

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
