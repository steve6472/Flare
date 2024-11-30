package steve6472.test;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.bullet.util.NativeLibrary;
import com.jme3.system.NativeLibraryLoader;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.core.registry.Key;
import steve6472.core.setting.SettingsLoader;
import steve6472.flare.Camera;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.core.FlareApp;
import steve6472.flare.input.KeybindUpdater;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.render.UIRenderSystem;
import steve6472.flare.ui.font.render.*;

import java.io.File;
import java.util.logging.Level;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestApp extends FlareApp
{
    private static final File TEST_SETTINGS = new File("settings/test_settings.json");

    @Override
    protected void preInit()
    {
        PhysicsSpace.logger.setLevel(Level.WARNING);
        PhysicsRigidBody.logger2.setLevel(Level.WARNING);
        NativeLibraryLoader.logger.setLevel(Level.WARNING);
        // TODO: remove bullet from Flare, use this exporting feature in Orbiter. Move the exporting into a separate util class
        NativeLibraryLoader.loadLibbulletjme(true, new File("generated/flare"), "Debug", "Sp");
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
//        addRenderSystem(new StaticModelRenderSystem(masterRenderer(), new PhysicsTestRender(), Pipelines.BLOCKBENCH_STATIC));
        addRenderSystem(UIRenderSystem::new, Pipelines.UI_TEXTURE);
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
        frameInfo.camera().setViewTarget(new Vector3f(-0.5f, 1.0f, 1), new Vector3f(0, 0.5f, 0));
        Vector2i mousePos = input().getMousePositionRelativeToTopLeftOfTheWindow();
        frameInfo.camera().setPerspectiveProjection(TestSettings.FOV.get(), aspectRatio(), 0.1f, 1024f);
        if (window().isFocused())
        {
            frameInfo.camera().center.set(0, 0, 0 - Y);
            frameInfo.camera().headOrbit(mousePos.x, mousePos.y, 0.4f, 0.8f);
        }

        float speed = 1f;

        if (TestKeybinds.LEFT.isActive())
            speed *= 10f;

        if (TestKeybinds.FORWARD.isActive())
            Y += frameInfo.frameTime() * speed;

        if (TestKeybinds.BACK.isActive())
            Y -= frameInfo.frameTime() * speed;

        Key sans = Key.withNamespace("test", "default_comic_sans");
        Key debug = Key.withNamespace("test", "debug");
        Key digi = Key.withNamespace("test", "digi");


//                text().line(TextLine.fromText("Rainbow in a Pot", 1f), new Matrix4f().translate(0, 0, 0));
//        text().message(new TextMessage(List.of(TextLine.fromText("Rainbow in a Pot", 1f)), 1f, 4f, Anchor.CENTER, Billboard.FIXED, Align.CENTER));
//        text().message(new TextMessage(List.of(
//            TextLine.fromText("Rainbow ", -1f),
//            TextLine.fromText("in ", -1f),
//            TextLine.fromText("a ", -1f),
//            TextLine.fromText("Pot ", -1f, debug),
//            TextLine.fromText("Sounds very yummy :)", -1f, sans)
//        ), 0.5f, 4f, Anchor.CENTER, Billboard.FIXED, Align.CENTER));
//        text().message(new TextMessage(List.of(
//            TextLine.fromText("Hello world Hello world", -1f)
//        ), 0.5f, 3f, Anchor.CENTER, Billboard.FIXED, Align.CENTER));
//        text().line(TextLine.fromText("Helloě world", 1f)); // ě is an unknown character in the digi font
//        text().line(TextLine.fromText("Comic Sans MS", 1f, Key.defaultNamespace("default_comic_sans")));
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
        return "Test Flare App";
    }

    @Override
    public String defaultNamespace()
    {
        return "base_test";
    }
}
