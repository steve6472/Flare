package steve6472.test;

import steve6472.flare.core.Flare;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class TestMain
{
    public static void main(String[] args)
    {
        System.setProperty("joml.format", "false");
        Flare.start(new TestApp());
    }
}
