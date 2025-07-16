package steve6472.test;

import steve6472.core.registry.Key;
import steve6472.flare.render.impl.UIRenderImpl;

/**
 * Created by steve6472
 * Date: 12/1/2024
 * Project: Flare <br>
 */
public class TestUIRender extends UIRenderImpl
{
    @Override
    public void render()
    {
        // Test animated texture
        int scale = 16;
        sprite(0, 0, 0, 8 * scale, 8 * scale, 8, 8, Key.withNamespace("test", "box_background"));

//        sprite(TestApp.instance.masterRenderer().getWindow().getWidth() - TestApp.pixelW * 10, 0, 0, TestApp.pixelW * 10, TestApp.pixelH * 10, TestApp.pixelW, TestApp.pixelH, Key.withNamespace("test", "box"));
    }
}
