package steve6472.flare.assets.model.blockbench;

import com.mojang.datafixers.util.Pair;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;
import steve6472.core.module.ModulePart;
import steve6472.core.registry.Key;
import steve6472.core.registry.ObjectRegistry;
import steve6472.core.util.ImagePacker;
import steve6472.flare.Commands;
import steve6472.flare.FlareParts;
import steve6472.flare.core.Flare;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.assets.model.Model;
import steve6472.flare.assets.model.primitive.PrimitiveModel;

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
        FlareRegistries.ANIMATED_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = FlareRegistries.ANIMATED_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        FlareRegistries.STATIC_LOADED_MODEL.keys().forEach(key ->
        {
            LoadedModel model = FlareRegistries.STATIC_LOADED_MODEL.get(key);
            model.elements().forEach(el -> el.fixUvs(model, imagePacker));
        });

        ErrorModel.INSTANCE.elements().forEach(el -> el.fixUvs(ErrorModel.INSTANCE, imagePacker));
    }

    public static void loadStaticModels()
    {
        loadModels(FlareParts.MODEL_STATIC, FlareRegistries.STATIC_LOADED_MODEL);
    }

    public static void loadAnimatedModels()
    {
        loadModels(FlareParts.MODEL_ANIMATED, FlareRegistries.ANIMATED_LOADED_MODEL);
    }

    public static Model createStaticModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ErrorModel.VK_STATIC_INSTANCE.createVertexBuffer(device, commands, graphicsQueue, ErrorModel.INSTANCE.toPrimitiveModel());
        FlareRegistries.STATIC_MODEL.register(ErrorModel.VK_STATIC_INSTANCE);

        return createModels(device, commands, graphicsQueue, FlareRegistries.STATIC_LOADED_MODEL, FlareRegistries.STATIC_MODEL, LoadedModel::toPrimitiveModel);
    }

    public static Model createAnimatedModels(VkDevice device, Commands commands, VkQueue graphicsQueue)
    {
        ErrorModel.VK_ANIMATED_INSTANCE.createVertexBuffer(device, commands, graphicsQueue, ErrorModel.INSTANCE.toPrimitiveSkinModel());
        FlareRegistries.ANIMATED_MODEL.register(ErrorModel.VK_ANIMATED_INSTANCE);

        return createModels(device, commands, graphicsQueue, FlareRegistries.ANIMATED_LOADED_MODEL, FlareRegistries.ANIMATED_MODEL, LoadedModel::toPrimitiveSkinModel);
    }

    private static Model createModels(VkDevice device, Commands commands, VkQueue graphicsQueue, ObjectRegistry<LoadedModel> from, ObjectRegistry<Model> to, Function<LoadedModel, PrimitiveModel> converter)
    {
        Model first = null;
        for (Key key : from.keys())
        {
            LoadedModel loadedModel = from.get(key);
            Model model = new Model(key);
            model.createVertexBuffer(device, commands, graphicsQueue, converter.apply(loadedModel));
            if (first == null)
                first = model;
            to.register(model);
        }
        return first;
    }

    private static void loadModels(ModulePart part, ObjectRegistry<LoadedModel> modelRegistry)
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
            modelRegistry.register(model.key(), model);
        }
    }

    private static LoadedModel overrideKey(LoadedModel model, Key newKey)
    {
        return new LoadedModel(model.meta(), newKey, model.resolution(), model.elements(), model.outliner(), model.textures(), model.animations());
    }
}
