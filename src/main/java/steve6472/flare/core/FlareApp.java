package steve6472.flare.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.core.registry.Key;
import steve6472.core.registry.ObjectRegistry;
import steve6472.core.registry.Registry;
import steve6472.flare.Camera;
import steve6472.flare.MasterRenderer;
import steve6472.flare.assets.atlas.Atlas;
import steve6472.flare.assets.atlas.SpriteAtlas;
import steve6472.flare.input.UserInput;
import steve6472.flare.Window;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.AnimateTextureSystem;
import steve6472.flare.render.RenderSystem;
import steve6472.flare.ui.font.render.TextRender;
import steve6472.flare.vr.VrData;
import steve6472.flare.vr.VrInput;

import java.util.function.BiFunction;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
public abstract class FlareApp
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
    /// Call [FlareApp#initRegistry(Registry)] to make sure registries are filled
    protected abstract void initRegistries();
    public abstract void loadSettings();

    protected abstract void createRenderSystems();
    public abstract void postInit();

    public void beginFrame() {}
    public void endFrame() {}

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
