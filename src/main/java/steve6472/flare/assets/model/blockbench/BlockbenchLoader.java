package steve6472.flare.assets.model.blockbench;

import com.mojang.datafixers.util.Pair;
import steve6472.core.module.ModulePart;
import steve6472.core.registry.Key;
import steve6472.core.registry.Registry;
import steve6472.core.util.ImagePacker;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.primitive.PrimitiveModel;
import steve6472.flare.registry.VkSetup;

import java.util.*;
import java.util.function.Function;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public class BlockbenchLoader
{
    public static void fixModelUvs(ImagePacker imagePacker)
    {
        BuiltInFlareRegistries.ANIMATED_LOADED_MODEL.listElements().forEach(ref -> {
            LoadedModel model = ref.value();
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        BuiltInFlareRegistries.STATIC_LOADED_MODEL.listElements().forEach(ref -> {
            LoadedModel model = ref.value();
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        ErrorModel.INSTANCE.elements().forEach(el -> el.fixUvs(ErrorModel.INSTANCE, imagePacker));
    }

    public static void loadStaticModels(Registry<LoadedModel> registry)
    {
        loadModels(FlareParts.MODEL_STATIC, registry);
    }

    public static void loadAnimatedModels(Registry<LoadedModel> registry)
    {
        loadModels(FlareParts.MODEL_ANIMATED, registry);
    }

    public static void createStaticModels(Registry<Model> registry, VkSetup setup)
    {
        ErrorModel.VK_STATIC_INSTANCE.createVertexBuffer(setup.device(), setup.commands(), setup.graphicsQueue(), ErrorModel.INSTANCE.toPrimitiveModel());
        Registry.register(registry, ErrorModel.KEY, ErrorModel.VK_STATIC_INSTANCE);

        createModels(setup, BuiltInFlareRegistries.STATIC_LOADED_MODEL, registry, LoadedModel::toPrimitiveModel);
    }

    public static void createAnimatedModels(Registry<Model> registry, VkSetup setup)
    {
        ErrorModel.VK_ANIMATED_INSTANCE.createVertexBuffer(setup.device(), setup.commands(), setup.graphicsQueue(), ErrorModel.INSTANCE.toPrimitiveSkinModel());
        Registry.register(registry, ErrorModel.KEY, ErrorModel.VK_ANIMATED_INSTANCE);

        createModels(setup, BuiltInFlareRegistries.ANIMATED_LOADED_MODEL, registry, LoadedModel::toPrimitiveSkinModel);
    }

    private static void createModels(VkSetup setup, Registry<LoadedModel> from, Registry<Model> to, Function<LoadedModel, PrimitiveModel> converter)
    {
        from.listElements().forEach(ref -> {
            LoadedModel loadedModel = ref.value();
            Model model = new Model(ref.key().resource());
            model.createVertexBuffer(setup.device(), setup.commands(), setup.graphicsQueue(), converter.apply(loadedModel));
            Registry.register(to, ref.key().resource(), model);
        });
    }

    private static void loadModels(ModulePart<LoadedModel> part, Registry<LoadedModel> registry)
    {
        Map<Key, Pair<LoadedModel, String>> models = new LinkedHashMap<>();

        Flare.getModuleManager().loadModuleJsonCodecs(part, LoadedModel.CODEC, (_, file, key, loadedModel) -> {
            key = Key.withNamespace(key.namespace(), part.path().substring("model/".length()) + "/" + key.id());
            loadedModel = overrideKey(loadedModel, key);
            models.put(key, Pair.of(loadedModel, file.getAbsolutePath()));
        });

        for (Pair<LoadedModel, String> value : models.values())
        {
            LoadedModel model = value.getFirst();
            Registry.register(registry, model.key(), model);
        }
    }

    private static LoadedModel overrideKey(LoadedModel model, Key newKey)
    {
        return new LoadedModel(model.meta(), newKey, model.resolution(), model.elements(), model.outliner(), model.textures(), model.animations());
    }
}
