package steve6472.volkaniums.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.core.registry.ObjectRegistry;
import steve6472.core.registry.Registry;
import steve6472.volkaniums.MasterRenderer;
import steve6472.volkaniums.input.UserInput;
import steve6472.volkaniums.Window;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.render.RenderSystem;

import java.util.function.BiFunction;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public abstract class VolkaniumsApp
{
    MasterRenderer masterRenderer;
    UserInput userInput;

    /*
     * Abstract methods
     * Init methods in order of execution
     */

    protected abstract void createRenderSystems();
    protected abstract void initRegistries();
    public abstract void fullInit();

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);

    public abstract void cleanup();

    /*
     * Abstract getters
     */

    public abstract String windowTitle();
    public abstract String defaultNamespace();

    /*
     * Protected methods, utils
     */

    protected final void addRenderSystem(BiFunction<MasterRenderer, PipelineConstructor, RenderSystem> renderSystemConstructor, PipelineConstructor pipeline)
    {
        masterRenderer.addRenderSystem(renderSystemConstructor.apply(masterRenderer, pipeline));
    }

    /// This method simply ensures that the fields in a static class are loaded.
    protected static void initRegistry(Registry<?> dummyRegistry) { }
    /// This method simply ensures that the fields in a static class are loaded.
    protected static void initRegistry(ObjectRegistry<?> dummyRegistry) { }

    /*
     * Global getters
     */

    public VkDevice device()  { return masterRenderer.getDevice(); }
    public UserInput input()  { return userInput; }
    public Window window()  { return masterRenderer.getWindow(); }
    public float aspectRatio()  { return masterRenderer.getAspectRatio(); }

    /*
     * Setup methods
     */

    final void createRenderSystems(MasterRenderer masterRenderer)
    {
        this.masterRenderer = masterRenderer;
        createRenderSystems();
    }
}
