package steve6472.test;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import steve6472.volkaniums.core.FrameInfo;
import steve6472.volkaniums.core.VolkaniumsApp;
import steve6472.volkaniums.pipeline.Pipelines;
import steve6472.volkaniums.render.BBStaticModelRenderSystem;
import steve6472.volkaniums.settings.VisualSettings;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
public class TestApp extends VolkaniumsApp
{
    @Override
    protected void createRenderSystems()
    {
        addRenderSystem(BBStaticModelRenderSystem::new, Pipelines.BB_STATIC);
    }

    @Override
    protected void initRegistries()
    {
        initRegistry(TestRegistries.RARITY);
    }

    float Y = 0;

    @Override
    public void render(FrameInfo frameInfo, MemoryStack stack)
    {
        frameInfo.camera().setViewTarget(new Vector3f(1f, 1.5f, -1), new Vector3f(0, 0.5f, 0));
        Vector2i mousePos = input().getMousePositionRelativeToTopLeftOfTheWindow();
        frameInfo.camera().setPerspectiveProjection(VisualSettings.FOV.get(), aspectRatio(), 0.1f, 1024f);
        if (window().isFocused())
        {
            frameInfo.camera().center.set(0, 0f + Y, 0);
            frameInfo.camera().headOrbit(mousePos.x, mousePos.y, 0.4f, 2.5f);
        }

        float speed = 4f;

        if (input().isKeyPressed(TestSettings.KEY_MOVE_LEFT))
            speed *= 10f;

        if (input().isKeyPressed(TestSettings.KEY_MOVE_FORWARD))
            Y += frameInfo.frameTime() * speed;

        if (input().isKeyPressed(TestSettings.KEY_MOVE_BACKWARD))
            Y -= frameInfo.frameTime() * speed;
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
