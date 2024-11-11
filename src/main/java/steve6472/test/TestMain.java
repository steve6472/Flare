package steve6472.test;

import steve6472.volkaniums.core.Volkaniums;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Volkaniums <br>
 */
class TestMain
{
    public static void main(String[] args)
    {
        System.setProperty("joml.format", "false");
        Volkaniums.start(new TestApp());
    }
}
