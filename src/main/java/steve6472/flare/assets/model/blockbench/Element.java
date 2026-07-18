package steve6472.flare.assets.model.blockbench;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.joml.Vector2f;
import org.joml.Vector3f;
import steve6472.core.registry.Registry;
import steve6472.core.registry.Typed;
import steve6472.core.util.ImagePacker;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.model.blockbench.element.CubeElement;
import steve6472.flare.assets.model.blockbench.element.LocatorElement;
import steve6472.flare.assets.model.blockbench.element.MeshElement;
import steve6472.flare.assets.model.blockbench.element.NullObjectElement;
import steve6472.flare.registry.BuiltInFlareRegistries;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by steve6472
 * Date: 8/17/2024
 * Project: Flare <br>
 */
public interface Element extends Typed<Element>
{
    Codec<Element> CODEC = BuiltInFlareRegistries.MODEL_ELEMENT.byKeyCodec().dispatch(Element::codec, Function.identity());

    static void bootstrap(Registry<MapCodec<? extends Element>> registry)
    {
        Registry.register(registry, "cube", CubeElement.CODEC);
        Registry.register(registry, "mesh", MeshElement.CODEC);
        Registry.register(registry, "locator", LocatorElement.CODEC);
        Registry.register(registry, "null_object", NullObjectElement.CODEC);
    }

    UUID uuid();
    String name();

    void fixUvs(LoadedModel model, Atlas atlas);

    List<Vector3f> toVertices();
    List<Vector3f> toNormals();
    List<Vector2f> toUVs();
}
