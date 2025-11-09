package steve6472.flare.pipeline.shader;

import org.lwjgl.vulkan.VkDevice;
import steve6472.core.log.Log;
import steve6472.flare.FlareConstants;
import steve6472.flare.ShaderSPIRVUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by steve6472
 * Date: 11/8/2025
 * Project: Flare <br>
 */
public class ShaderCache
{
    private static final Map<ShaderId, Shader> SHADER_CACHE = new HashMap<>();
    private static final MessageDigest MD5 = getMd5();

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

    public static ShaderSPIRVUtils.SPIRV getOrCompileShader(String shaderFile, ShaderSPIRVUtils.ShaderKind shaderKind)
    {
        InputStream inputStream = ShaderSPIRVUtils.class.getClassLoader().getResourceAsStream(shaderFile);

        if (inputStream == null)
        {
            throw new RuntimeException("Shader file not found: " + shaderFile);
        }

        String source;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            source = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        String hash = computeMD5(source);
        File cachedShaderFile = new File(FlareConstants.SHADER_CACHE, hash);
        if (cachedShaderFile.exists())
        {
            try
            {
                byte[] binary = Files.readAllBytes(cachedShaderFile.toPath());
                ByteBuffer wrap = ByteBuffer.allocateDirect(binary.length);
                wrap.put(binary);
                wrap.position(0);
//                System.out.println("Reading: " + hash + " -> " + wrap + " -> " + Arrays.toString(binary));
                return new ShaderSPIRVUtils.SPIRV(wrap);
            } catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        ShaderSPIRVUtils.SPIRV spirv = ShaderSPIRVUtils.compileShader(shaderFile, source, shaderKind);

        try
        {
            ByteBuffer bytecode = spirv.bytecode();
            byte[] bytecodeArray = new byte[bytecode.capacity()];
            bytecode.get(bytecodeArray);
            bytecode.position(0);
//            System.out.println("Saving: " + hash + " -> " + bytecode + " -> " + Arrays.toString(bytecodeArray));
            Files.write(cachedShaderFile.toPath(), bytecodeArray);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return spirv;
    }

    private static String computeMD5(String text)
    {
        byte[] digest = MD5.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest)
        {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static MessageDigest getMd5()
    {
        try
        {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
}
