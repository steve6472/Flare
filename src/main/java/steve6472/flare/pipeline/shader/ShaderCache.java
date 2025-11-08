package steve6472.flare.pipeline.shader;

import org.lwjgl.vulkan.VkDevice;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve6472
 * Date: 11/8/2025
 * Project: Flare <br>
 */
public class ShaderCache
{
    private static Map<ShaderId, Shader> SHADER_CACHE = new HashMap<>();

    public static Shader getShader(ShaderId shaderId)
    {
        return SHADER_CACHE.computeIfAbsent(shaderId, Shader::new);
    }

    public static void cleanup(VkDevice device)
    {
        for (Shader shader : SHADER_CACHE.values())
        {
            shader.cleanup(device);
        }
    }
}
