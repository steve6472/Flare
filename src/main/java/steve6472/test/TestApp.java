package steve6472.test;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import steve6472.core.log.Log;
import steve6472.core.registry.Key;
import steve6472.core.setting.SettingsLoader;
import steve6472.flare.Camera;
import steve6472.flare.FlareConstants;
import steve6472.flare.assets.TextureSampler;
import steve6472.flare.core.FrameInfo;
import steve6472.flare.core.FlareApp;
import steve6472.flare.input.KeybindUpdater;
import steve6472.flare.pipeline.Pipelines;
import steve6472.flare.registry.FlareRegistries;
import steve6472.flare.render.*;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestApp extends FlareApp
{
    private static final Logger LOGGER = Log.getLogger(TestApp.class);
    private static final File TEST_SETTINGS = new File("settings/test_settings.json");
    public static TestApp instance;
    public static boolean DUMP_SAMPLERS = false;

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
//        addRenderSystem(new StaticModelRenderSystem(masterRenderer(), new EntityTestRender(), Pipelines.BLOCKBENCH_STATIC));
        addRenderSystem(new SkinRenderSystem(masterRenderer(), Pipelines.SKIN));
        addRenderSystem(new UIRenderSystem(masterRenderer(), new TestUIRender(), 256f));
        addRenderSystem(new UIFontRender(masterRenderer(), new TestFontRender()));
        addRenderSystem(new UILineRender(masterRenderer(), new DebugUILines()));

        window().callbacks().addCharCallback(key("char_input"), (_, codepoint) ->
        {
            TestFontRender.editableText += Character.toString(codepoint);
        });

        window().callbacks().addKeyCallback(key("key_input"), (_, key, scancode, action, mods) ->
        {
            if (action == GLFW.GLFW_RELEASE)
                return;

            if (key == GLFW.GLFW_KEY_BACKSPACE)
            {
                if (!TestFontRender.editableText.isEmpty())
                    TestFontRender.editableText = TestFontRender.editableText.substring(0, TestFontRender.editableText.length() - 1);
            }
        });
    }

    @Override
    public void postInit()
    {
        KeybindUpdater.updateKeybinds(TestRegistries.KEYBIND, input());

        if (DUMP_SAMPLERS)
            dumpSamplers();
    }

    private void dumpSamplers()
    {
        LOGGER.info("Dumping samplers");
        File file = getFile("/sampler");

        LOGGER.info("Generating new samplers");
        for (Key key : FlareRegistries.SAMPLER.keys())
        {
            LOGGER.info("Dumping " + key);
            File dumpFile = new File(file, key.namespace() + "-" + key.id().replaceAll("/", "__") + ".png");
            TextureSampler textureSampler = FlareRegistries.SAMPLER.get(key);
            textureSampler.texture.saveTextureAsPNG(device(), masterRenderer().getCommands(), masterRenderer().getGraphicsQueue(), dumpFile);
        }
        LOGGER.info("Finished dumping samplers");
    }

    private static @NotNull File getFile(String suffix)
    {
        File file = new File(FlareConstants.FLARE_DEBUG_FOLDER, "dumped" + suffix);
        if (file.exists())
        {
            LOGGER.info("Removing old textures");
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File listFile : files)
                {
                    if (!listFile.delete())
                    {
                        LOGGER.severe("Could not delete " + listFile.getAbsolutePath());
                    }
                }
            }
        } else
        {
            if (!file.mkdirs())
            {
                LOGGER.severe("Could not create " + file.getAbsolutePath());
            }
        }
        return file;
    }

    float X = 0;
    float Z = 0;
    float cameraDistance = 0.8f;
    boolean canControlCamera = true;

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
//        frameInfo.camera().setViewTarget(new Vector3f(-0.5f, 1.0f, 1), new Vector3f(0, 0.5f, 0));
        Vector2i mousePos = input().getMousePositionRelativeToTopLeftOfTheWindow();
        frameInfo.camera().setPerspectiveProjection(TestSettings.FOV.get(), aspectRatio(), 0.1f, 1024f);

        frameInfo.camera().center.set(X, 0, Z);

        if (canControlCamera)
        {
            frameInfo.camera().headOrbit(mousePos.x, mousePos.y, 0.4f, cameraDistance);
        } else
        {
            frameInfo.camera().oldx = mousePos.x;
            frameInfo.camera().oldy = mousePos.y;
        }

        float cameraSpeed = 0.06f;

        if (TestKeybinds.SPRINT.isActive())
            cameraSpeed *= 2f;

        if (TestKeybinds.SLOW.isActive())
            cameraSpeed /= 4f;

        if (TestKeybinds.CAMERA_FAR.isActive())
            cameraDistance += cameraSpeed;
        if (TestKeybinds.CAMERA_CLOSE.isActive())
            cameraDistance -= cameraSpeed;
        cameraDistance = Math.clamp(cameraDistance, 0, 16);

        if (TestKeybinds.TOGGLE_CAMERA_CONTROL.isActive())
        {
            canControlCamera = !canControlCamera;
        }

        float speed = 0.05f;

        if (TestKeybinds.SPRINT.isActive())
            speed *= 5f;

        if (TestKeybinds.SLOW.isActive())
            speed /= 5f;

        Camera camera = frameInfo.camera();

        if (TestKeybinds.FORWARD.isActive())
        {
            X += (float) (Math.sin(camera.yaw()) * -speed);
            Z += (float) (Math.cos(camera.yaw()) * -speed);
        }

        if (TestKeybinds.BACK.isActive())
        {
            X += (float) (Math.sin(camera.yaw()) * speed);
            Z += (float) (Math.cos(camera.yaw()) * speed);
        }

        if (TestKeybinds.LEFT.isActive())
        {
            X += (float) (Math.sin(camera.yaw() + Math.PI / 2.0) * -speed);
            Z += (float) (Math.cos(camera.yaw() + Math.PI / 2.0) * -speed);
        }

        if (TestKeybinds.RIGHT.isActive())
        {
            X += (float) (Math.sin(camera.yaw() + Math.PI / 2.0) * speed);
            Z += (float) (Math.cos(camera.yaw() + Math.PI / 2.0) * speed);
        }

        if (TestKeybinds.L.isActive())
        {
            dumpSamplers();
        }

        Key sans = Key.withNamespace("test", "default_comic_sans");
        Key debug = Key.withNamespace("test", "debug");
        Key digi = Key.withNamespace("test", "digi");

//                text().line(TextLine.fromText("Rainbow in a Pot", 0.25f), new Matrix4f().translate(0, 0.5f, 0.2f));
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

    public static Key key(String key)
    {
        return Key.withNamespace(instance.defaultNamespace(), key);
    }
}
