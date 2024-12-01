package steve6472.flare.module;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;
import steve6472.core.log.Log;
import steve6472.core.util.GsonUtil;
import steve6472.flare.FlareConstants;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 11/26/2024
 * Project: Flare <br>
 */
public class ModuleManager
{
    private static final Logger LOGGER = Log.getLogger(ModuleManager.class);
    private static final String[] MODULE_INFO = {"module_info.json5", "module_info.json"};

    private final List<Map.Entry<String, Module>> modules = new ArrayList<>();

    public void loadModules()
    {
        File[] modules = FlareConstants.MODULES.listFiles();
        Objects.requireNonNull(modules, "List files for Modules folder failed!");

        for (File moduleRoot : modules)
        {
            String rootName = moduleRoot.getName();
            File moduleInfo = getModuleInfo(moduleRoot);

            if (moduleInfo == null)
            {
                LOGGER.warning("Installed module '" + rootName + "' does not have module_info and will not be loaded!");
                continue;
            }

            JsonElement jsonElement = GsonUtil.loadJson(moduleInfo);
            DataResult<Pair<Module, JsonElement>> decode = Module.CODEC.decode(JsonOps.INSTANCE, jsonElement);
            Module module = decode.getOrThrow().getFirst();
            module.root = moduleRoot;
            module.namespaces = listNamespaces(moduleRoot);
            this.modules.add(Map.entry(rootName, module));

            LOGGER.info("Loaded module '" + rootName + "'");
        }

        fixLoadOrder();
    }

    private void fixLoadOrder()
    {
        Map.Entry<String, Module> flareModule = null;

        for (Map.Entry<String, Module> module : modules)
        {
            if (module.getKey().equals(FlareConstants.NAMESPACE))
            {
                flareModule = module;
                break;
            }
        }

        if (flareModule == null)
            throw new RuntimeException("Built-in Flare module not loaded!");

        // Ensure Flare module is always loaded first!
        modules.remove(flareModule);
        modules.addFirst(flareModule);
    }

    @Nullable
    public Module getModule(String namespace)
    {
        for (Map.Entry<String, Module> module : modules)
        {
            if (module.getKey().equals(namespace))
                return module.getValue();
        }

        return null;
    }

    /// Return a copy of a module list in the load order
    public List<Module> getModules()
    {
        return modules.stream().map(Map.Entry::getValue).toList();
    }

    @Nullable
    private File getModuleInfo(File module)
    {
        for (String moduleInfoFile : MODULE_INFO)
        {
            File moduleInfo = new File(module, moduleInfoFile);
            if (!moduleInfo.exists())
                continue;
            return moduleInfo;
        }

        // No module_info found!
        return null;
    }

    private List<String> listNamespaces(File module)
    {
        File[] files = module.listFiles();
        if (files == null)
            return List.of();
        List<String> namespaces = new ArrayList<>();
        for (File file : files)
        {
            if (file.isDirectory())
                namespaces.add(file.getName());
        }
        return List.copyOf(namespaces);
    }
}
