package steve6472.volkaniums.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.core.registry.ObjectRegistry;
import steve6472.core.registry.Registry;
import steve6472.volkaniums.Camera;
import steve6472.volkaniums.MasterRenderer;
import steve6472.volkaniums.input.UserInput;
import steve6472.volkaniums.Window;
import steve6472.volkaniums.pipeline.builder.PipelineConstructor;
import steve6472.volkaniums.render.RenderSystem;
import steve6472.volkaniums.ui.font.render.TextRender;
import steve6472.volkaniums.vr.VrData;
import steve6472.volkaniums.vr.VrInput;

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
    Camera camera;

    /*
     * Abstract methods
     * Init methods in order of execution
     */

    protected abstract void preInit();
    protected abstract Camera setupCamera();
    protected abstract void initRegistries();
    public abstract void loadSettings();

    protected abstract void createRenderSystems();
    public abstract void postInit();

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);

    public abstract void saveSettings();
    public abstract void cleanup();

    /*
     * Abstract getters
     */

    public abstract String windowTitle();
    public abstract String defaultNamespace();

    /*
     * Protected methods, utils
     */

    protected final <T extends RenderSystem> T addRenderSystem(BiFunction<MasterRenderer, PipelineConstructor, T> renderSystemConstructor, PipelineConstructor pipeline)
    {
        T renderSystem = renderSystemConstructor.apply(masterRenderer, pipeline);
        masterRenderer.addRenderSystem(renderSystem);
        return renderSystem;
    }

    protected final void addRenderSystem(RenderSystem renderSystem)
    {
        masterRenderer.addRenderSystem(renderSystem);
    }

    /// This method simply ensures that the fields in a static class are loaded.
    protected static void initRegistry(Registry<?> dummyRegistry) { }
    /// This method simply ensures that the fields in a static class are loaded.
    protected static void initRegistry(ObjectRegistry<?> dummyRegistry) { }

    /*
     * Global getters
     */

    public VkDevice device()  { return masterRenderer.getDevice(); }
    public VrData vrData() { return masterRenderer.getVrData(); }
    public UserInput input()  { return userInput; }
    public VrInput vrInput()  { return vrData().vrInput(); }
    public Window window()  { return masterRenderer.getWindow(); }
    public float aspectRatio()  { return masterRenderer.getAspectRatio(); }
    public MasterRenderer masterRenderer() { return masterRenderer; }
    public Camera camera() { return camera; }
    public TextRender text() { return masterRenderer.textRender(); }

    /*
     * Setup methods
     */

    final void createRenderSystems(MasterRenderer masterRenderer)
    {
        this.masterRenderer = masterRenderer;
        createRenderSystems();
    }
}
