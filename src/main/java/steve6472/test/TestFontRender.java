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
    public static boolean cursorInWindow = false;
    public static String editableText = "the quick brown fox jumps over a lazy dog";
//    public static String editableText = "ji~";

    public TestFontRender()
    {
        super(256f);
        TestApp.instance.window().callbacks().addCursorEnterCallback(Key.withNamespace("base_test", "font_render_switch"), (w, b) -> {
            cursorInWindow = b;
        });
    }

    @Override
    public void render()
    {
        FontStyleEntry arial = getStyleEntry(Key.withNamespace("flare", "arial"));
        FontStyleEntry comicSans = getStyleEntry(Key.withNamespace("test", "default_comic_sans"));
        FontStyleEntry digi = getStyleEntry(Key.withNamespace("test", "digi"));
//        renderLine(new UITextLine("Moved from overload method", 16f, arial), 100, 30);
//        renderLine("Hello World!", 64f, arial, Anchor2D.TOP_LEFT, new Matrix4f().translate(0, 0, 0));
//        renderMessage(new UITextMessage(List.of(new UITextLine("Hello world! How do you fare?", 64f, arial)), 64f, 128, Anchor2D.TOP_LEFT, Align.LEFT, NewLineType.MAX_HEIGHT, 0f), new Matrix4f());
//        renderMessage(new UITextMessage(List.of(new UITextLine("Hello world! How do you fare?", arial)), 24f, 128, Anchor2D.TOP_LEFT, Align.LEFT, NewLineType.MAX_HEIGHT, 0), new Matrix4f());
//        renderMessage(new UITextMessage(List.of(new UITextLine("Hello world!", arial), new UITextLine("HOW", 32f * 2f, arial), new UITextLine(" do you fare?", arial)), 24f * 2f, 128 * 2f, Anchor2D.CENTER, Align.CENTER, NewLineType.MAX_HEIGHT, 0), new Matrix4f());

        Anchor2D anchor = Anchor2D.CENTER;
        Align align = Align.CENTER;

//        renderMessage(new UITextMessage(List.of(new UITextLine(editableText, arial)), 24f * 4f, 128 * 4f, anchor, align, NewLineType.MAX_HEIGHT, 0), new Matrix4f().translate(20, 50, 0));
        renderMessage(
            new UITextMessage(
                List.of(
                    new UITextLine("I ", 24f * 2f, arial),
                    new UITextLine("have  ", 24f * 2f, digi),
                    new UITextLine("FAILED", 24f * 4f, arial),
                    new UITextLine(" this ", 24f * 2f, arial),
                    new UITextLine("SPECTACULARLY", 24f * 2f, comicSans)
                ), 24f, 128 * 5f, anchor, align, NewLineType.MAX_HEIGHT, 0), new Matrix4f().translate(20, 50, 0));
//        renderMessage(new UITextMessage(List.of(new UITextLine(editableText, arial)), 24f * 16f, 128 * 6f, anchor, align, NewLineType.MAX_HEIGHT, 0), new Matrix4f().translate(20, 20, 0));

//        renderMessage(new UITextMessage(List.of(new UITextLine(".", arial)), 24f * 2f, 128 * 2f, Anchor2D.CENTER, Align.CENTER, NewLineType.MAX_HEIGHT, 0), new Matrix4f().translate(200, 100, 0));
    }
}
