package steve6472.test;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.setting.SettingsLoader;
import steve6472.volkaniums.Camera;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.core.VolkaniumsApp;
import steve6472.volkaniums.input.KeybindUpdater;
import steve6472.volkaniums.pipeline.Pipelines;
import steve6472.volkaniums.render.StaticModelRenderSystem;

import java.io.File;
import java.util.logging.Level;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public class TestApp extends VolkaniumsApp
{
    private static final File TEST_SETTINGS = new File("settings/test_settings.json");

    @Override
    protected void preInit()
    {
        PhysicsSpace.logger.setLevel(Level.WARNING);
        PhysicsRigidBody.logger2.setLevel(Level.WARNING);
        NativeLibraryLoader.logger.setLevel(Level.WARNING);
        NativeLibraryLoader.loadLibbulletjme(true, new File("dep"), "Debug", "Sp");
        NativeLibrary.setStartupMessageEnabled(false);
    }

    @Override
    protected Camera setupCamera()
    {
        return new Camera();
    }

    @Override
    protected void initRegistries()
    {
        initRegistry(TestRegistries.RARITY);
    }

    @Override
    public void loadSettings()
    {
        SettingsLoader.loadFromJsonFile(TestRegistries.SETTING, TEST_SETTINGS);
    }

    @Override
    protected void createRenderSystems()
    {
        addRenderSystem(new StaticModelRenderSystem(masterRenderer(), new PhysicsTestRender(), Pipelines.BLOCKBENCH_STATIC));
    }

    @Override
    public void postInit()
    {
        KeybindUpdater.updateKeybinds(TestRegistries.KEYBIND, input());
    }

    float Y = 0;

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        frameInfo.camera().setViewTarget(new Vector3f(1f, 1.5f, -1), new Vector3f(0, 0.5f, 0));
        Vector2i mousePos = input().getMousePositionRelativeToTopLeftOfTheWindow();
        frameInfo.camera().setPerspectiveProjection(TestSettings.FOV.get(), aspectRatio(), 0.1f, 1024f);
        if (window().isFocused())
        {
            frameInfo.camera().center.set(0, 0f + Y, 0);
            frameInfo.camera().headOrbit(mousePos.x, mousePos.y, 0.4f, 2.5f);
        }

        float speed = 4f;

        if (TestKeybinds.LEFT.isActive())
            speed *= 10f;

        if (TestKeybinds.FORWARD.isActive())
            Y += frameInfo.frameTime() * speed;

        if (TestKeybinds.BACK.isActive())
            Y -= frameInfo.frameTime() * speed;
    }

    @Override
    public void saveSettings()
    {
        SettingsLoader.saveToJsonFile(TestRegistries.SETTING, TEST_SETTINGS);
    }

    @Override
    public void cleanup()
    {

    }

    @Override
    public String windowTitle()
    {
        return "Test Volkaniums App";
    }

    @Override
    public String defaultNamespace()
    {
        return "base_test";
    }
}
