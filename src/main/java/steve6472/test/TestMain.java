package steve6472.test;

import steve6472.flare.assets.atlas.SpriteLoader;
import steve6472.flare.core.Flare;

import java.io.IOException;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestMain
{
    public static void main(String[] args) throws IOException
    {
        SpriteLoader.SAVE_DEBUG_ATLASES = true;
        TestApp.DUMP_SAMPLERS = false;

        System.setProperty("joml.format", "false");
        Flare.start(new TestApp());
    }
}
