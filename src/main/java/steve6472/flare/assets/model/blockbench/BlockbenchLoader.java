package steve6472.flare.assets.model.blockbench;

import steve6472.core.log.Log;
import steve6472.core.module.ModulePart;
import steve6472.core.registry.Key;
import steve6472.core.registry.Registry;
import steve6472.flare.FlareParts;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.BuiltInFlareRegistries;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.primitive.PrimitiveModel;
import steve6472.flare.registry.VkSetup;
import steve6472.flare.tracy.FlareProfiler;
import steve6472.flare.tracy.Profiler;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/22/2024
 * Project: Flare <br>
 */
public class BlockbenchLoader
{
    private static final Logger LOGGER = Log.getLogger(BlockbenchLoader.class);

    public static void fixModelUvs(Atlas atlas)
    {
        BuiltInFlareRegistries.ANIMATED_LOADED_MODEL.listElements().forEach(ref -> {
            LoadedModel model = ref.value();
            model.elements().forEach(el -> el.fixUvs(model, atlas));
        });

        BuiltInFlareRegistries.STATIC_LOADED_MODEL.listElements().forEach(ref -> {
            LoadedModel model = ref.value();
            model.elements().forEach(el -> el.fixUvs(model, atlas));
        });

        ErrorModel.INSTANCE.elements().forEach(el -> el.fixUvs(ErrorModel.INSTANCE, atlas));
    }

    public static void loadStaticModels(Registry<LoadedModel> registry)
    {
        Profiler profiler = FlareProfiler.frame();
        profiler.push("staticModels");
        loadModels(FlareParts.MODEL_STATIC, registry);
        profiler.pop();
    }

    public static void loadAnimatedModels(Registry<LoadedModel> registry)
    {
        Profiler profiler = FlareProfiler.frame();
        profiler.push("animatedModels");
        loadModels(FlareParts.MODEL_ANIMATED, registry);
        profiler.pop();
    }

    public static void createStaticModels(Registry<Model> registry, VkSetup setup)
    {
        Profiler frame = FlareProfiler.frame();
        frame.push("createStaticModels");
        if (ErrorModel.VK_STATIC_INSTANCE.vertexBuffer == null)
            ErrorModel.VK_STATIC_INSTANCE.createVertexBuffer(setup.device(), setup.commands(), setup.graphicsQueue(), ErrorModel.INSTANCE.toPrimitiveModel());
        Registry.register(registry, ErrorModel.KEY, ErrorModel.VK_STATIC_INSTANCE);

        createModels(setup, BuiltInFlareRegistries.STATIC_LOADED_MODEL, registry, LoadedModel::toPrimitiveModel);
        frame.pop();
    }

    public static void createAnimatedModels(Registry<Model> registry, VkSetup setup)
    {
        Profiler frame = FlareProfiler.frame();
        frame.push("createAnimatedModels");
        if (ErrorModel.VK_ANIMATED_INSTANCE.vertexBuffer == null)
            ErrorModel.VK_ANIMATED_INSTANCE.createVertexBuffer(setup.device(), setup.commands(), setup.graphicsQueue(), ErrorModel.INSTANCE.toPrimitiveSkinModel());
        Registry.register(registry, ErrorModel.KEY, ErrorModel.VK_ANIMATED_INSTANCE);

        createModels(setup, BuiltInFlareRegistries.ANIMATED_LOADED_MODEL, registry, LoadedModel::toPrimitiveSkinModel);
        frame.pop();
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
        Flare.getModuleManager().loadModuleJsonCodecs(part, LoadedModel.CODEC, (_, file, key, loadedModel) -> {
            key = Key.withNamespace(key.namespace(), part.path().substring("model/".length()) + "/" + key.id());
            loadedModel = overrideKey(loadedModel, key);
            Registry.register(registry, key, loadedModel);
        }, (ex, key) -> Log.exception(LOGGER, ex, "Failed to load Model '" + key + "'"));
    }

    private static LoadedModel overrideKey(LoadedModel model, Key newKey)
    {
        return new LoadedModel(model.meta(), newKey, model.resolution(), model.elements(), model.outliner(), model.textures(), model.animations());
    }
}
