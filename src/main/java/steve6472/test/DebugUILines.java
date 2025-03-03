package steve6472.test;

import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector4f;
import steve6472.core.setting.BoolSetting;
import steve6472.flare.render.debug.DebugRender;
import steve6472.flare.render.impl.UILineRenderImpl;
import steve6472.flare.settings.FontDebugSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve6472
 * Date: 12/19/2024
 * Project: MoonDust <br>
 */
public class DebugUILines extends UILineRenderImpl
{
    private static final List<DebugOption> OPTIONS = new ArrayList<>();

    public static final DebugOption BASELINE = new DebugOption(FontDebugSettings.BASELINE, DebugRender.LIGHT_GREEN);

    public static final DebugOption CHARACTER = new DebugOption(FontDebugSettings.CHARACTER, DebugRender.YELLOW);
    public static final DebugOption CHARACTER_ASCENT = new DebugOption(FontDebugSettings.CHARACTER_ASCENT, DebugRender.ORANGE);
    public static final DebugOption CHARACTER_DESCENT = new DebugOption(FontDebugSettings.CHARACTER_DESCENT, DebugRender.LIME);
    public static final DebugOption CHARACTER_ADVANCE = new DebugOption(FontDebugSettings.CHARACTER_ADVANCE, DebugRender.GOLD);
    public static final DebugOption CHARACTER_KERN = new DebugOption(FontDebugSettings.CHARACTER_KERN, DebugRender.PLUM);
    public static final DebugOption CHARACTER_UNDERLINE = new DebugOption(FontDebugSettings.CHARACTER_UNDERLINE, DebugRender.CORAL);

    public static final DebugOption SEGMENT = new DebugOption(FontDebugSettings.SEGMENT, DebugRender.DARK_MAGENTA);
    public static final DebugOption SEGMENT_MIN_DESCENT = new DebugOption(FontDebugSettings.SEGMENT_MIN_DESCENT, DebugRender.DARK_BLUE);
    public static final DebugOption SEGMENT_MAX_DESCENT = new DebugOption(FontDebugSettings.SEGMENT_MAX_DESCENT, DebugRender.PURPLE);

    public static final DebugOption MESSAGE_ORIGIN = new DebugOption(FontDebugSettings.MESSAGE_ORIGIN, DebugRender.GREEN);
    public static final DebugOption MESSAGE_MAX_WIDTH = new DebugOption(FontDebugSettings.MESSAGE_MAX_WIDTH, DebugRender.KHAKI);
    public static final DebugOption MESSAGE_ANCHORS = new DebugOption(FontDebugSettings.MESSAGE_ANCHORS, DebugRender.DARK_GREEN);

    private void render(DebugOption option)
    {
        if (option.get())
        {
            for (int i = 0; i < option.vertexList.size() / 2; i++)
            {
                Vector2i start = option.vertexList.get(i * 2);
                Vector2i end = option.vertexList.get(i * 2 + 1);
                // start and end are already scaled
                line(start, end, option.color);
            }
            option.vertexList.clear();
        }
    }

    @Override
    public void render()
    {
        for (DebugOption option : OPTIONS)
        {
            render(option);
        }
    }

    private void line(Vector2i start, Vector2i end, Vector4f color)
    {
        line(new Vector3f(start.x, start.y, 0), new Vector3f(end.x, end.y, 0), color);
    }

    public record DebugOption(List<Vector2i> vertexList, BoolSetting setting, Vector4f color)
    {
        public DebugOption
        {
            OPTIONS.add(this);
        }

        public DebugOption(BoolSetting setting, Vector4f color)
        {
            this(new ArrayList<>(1024), setting, color);
        }

        public boolean get()
        {
            return setting().get();
        }

        public void rectangle(Vector2i start, Vector2i end)
        {
            if (!get()) return;

            // TOP
            vertexList.add(new Vector2i(start.x, start.y));
            vertexList.add(new Vector2i(end.x, start.y));

            // BOTTOM
            vertexList.add(new Vector2i(start.x, end.y));
            vertexList.add(new Vector2i(end.x, end.y));

            // LEFT
            vertexList.add(new Vector2i(start.x, start.y - 1));
            vertexList.add(new Vector2i(start.x, end.y));

            // RIGHT
            vertexList.add(new Vector2i(end.x, start.y));
            vertexList.add(new Vector2i(end.x, end.y));
        }

        public void line(Vector2i start, Vector2i end)
        {
            if (!get()) return;

            vertexList.add(new Vector2i(start));
            vertexList.add(new Vector2i(end));
        }
    }
}
