package steve6472.test;

import org.joml.Matrix4f;
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
import steve6472.flare.render.StaticModelRenderSystem;
import steve6472.flare.render.UIFontRender;
import steve6472.flare.render.UIRenderSystem;
import steve6472.flare.ui.font.render.*;

import java.io.File;
import java.util.List;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestApp extends FlareApp
{
    private static final File TEST_SETTINGS = new File("settings/test_settings.json");
    public static TestApp instance;

    @Override
    protected void preInit()
    {
        instance = this;
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
        addRenderSystem(new StaticModelRenderSystem(masterRenderer(), new EntityTestRender(), Pipelines.BLOCKBENCH_STATIC));
//        addRenderSystem(new UIRenderSystem(masterRenderer(), new TestUIRender(), 256f));
        addRenderSystem(new UIFontRender(masterRenderer(), new TestFontRender()));
    }

    @Override
    public void postInit()
    {
        KeybindUpdater.updateKeybinds(TestRegistries.KEYBIND, input());
    }

    float Y = 0;

    public static int pixelW = 20;
    public static int pixelH = 20;

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

        if (TestKeybinds.TO_UP.isActive()) pixelH -= 1;
        if (TestKeybinds.TO_DOWN.isActive()) pixelH += 1;
        if (TestKeybinds.TO_LEFT.isActive()) pixelW -= 1;
        if (TestKeybinds.TO_RIGHT.isActive()) pixelW += 1;

        Key sans = Key.withNamespace("test", "default_comic_sans");
        Key debug = Key.withNamespace("test", "debug");
        Key digi = Key.withNamespace("test", "digi");


                text().line(TextLine.fromText("Rainbow in a Pot", 0.25f), new Matrix4f().translate(0, 0.5f, 0.2f));
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
//        text().line(TextLine.fromText("Comic Sans MS", 1f, digi));
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
