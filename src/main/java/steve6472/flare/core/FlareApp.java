package steve6472.flare.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import steve6472.flare.Camera;
import steve6472.flare.MasterRenderer;
import steve6472.flare.input.UserInput;
import steve6472.flare.Window;
import steve6472.flare.pipeline.builder.PipelineConstructor;
import steve6472.flare.render.RenderSystem;
import steve6472.flare.ui.font.render.TextRender;

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

    protected abstract void setup(Setup events);

    protected abstract Camera setupCamera();

    protected abstract void createRenderSystems();
    public abstract void postInit();

    public abstract void render(FrameInfo frameInfo, MemoryStack stack);

    public abstract void saveSettings();
    public abstract void cleanup();

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

    /*
     * Global getters
     */

    public VkDevice device()  { return masterRenderer.getDevice(); }
    public UserInput input()  { return userInput; }
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
