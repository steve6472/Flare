package steve6472.test;

import org.joml.Matrix4f;
import steve6472.core.registry.Key;
import steve6472.flare.render.impl.UIFontRenderImpl;
import steve6472.flare.ui.font.render.*;
import steve6472.flare.ui.font.style.FontStyleEntry;

import java.util.List;

/**
 * Created by steve6472
 * Date: 12/12/2024
 * Project: Flare <br>
 */
public class TestFontRender extends UIFontRenderImpl
{
    public TestFontRender()
    {
        super(256f);
    }

    @Override
    public void render()
    {
        FontStyleEntry arial = getStyleEntry(Key.withNamespace("flare", "arial"));
//        renderLine(new UITextLine("Moved from overload method", 16f, arial), 100, 30);
//        renderLine("Hello World!", 64f, arial, Anchor2D.TOP_LEFT, new Matrix4f().translate(0, 0, 0));
//        renderMessage(new UITextMessage(List.of(new UITextLine("Hello world! How do you fare?", 64f, arial)), 64f, 128, Anchor2D.TOP_LEFT, Align.LEFT, NewLineType.MAX_HEIGHT, 0f), new Matrix4f());
//        renderMessage(new UITextMessage(List.of(new UITextLine("Hello world! How do you fare?", arial)), 24f, 128, Anchor2D.TOP_LEFT, Align.LEFT, NewLineType.MAX_HEIGHT, 0), new Matrix4f());
        renderMessage(new UITextMessage(List.of(new UITextLine("Hello world!", arial), new UITextLine("HOW", 32f * 2f, arial), new UITextLine(" do you fare?", arial)), 24f * 2f, 128 * 2f, Anchor2D.TOP_LEFT, Align.LEFT, NewLineType.MAX_HEIGHT, 0), new Matrix4f());
    }
}
