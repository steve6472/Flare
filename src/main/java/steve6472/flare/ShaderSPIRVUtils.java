package steve6472.flare;

import org.lwjgl.system.NativeResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

public class ShaderSPIRVUtils
{
    public static SPIRV compileShaderFile(String shaderFile, ShaderKind shaderKind)
    {
        InputStream inputStream = ShaderSPIRVUtils.class.getClassLoader().getResourceAsStream(shaderFile);

        if (inputStream == null)
        {
            throw new RuntimeException("Shader file not found: " + shaderFile);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)))
        {
            String source = reader.lines().collect(Collectors.joining("\n"));
            return compileShader(shaderFile, source, shaderKind);
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static SPIRV compileShader(String filename, String source, ShaderKind shaderKind)
    {
        long compiler = shaderc_compiler_initialize();

        if (compiler == NULL)
        {
            throw new RuntimeException("Failed to create shader compiler");
        }

        long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL);

        if (result == NULL)
        {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V");
        }

        if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success)
        {
            throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V:\n " + shaderc_result_get_error_message(result));
        }

        shaderc_compiler_release(compiler);

        return new SPIRV(result, shaderc_result_get_bytes(result));
    }

    public enum ShaderKind
    {
        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader),
        COMPUTE_SHADER(shaderc_glsl_compute_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader);

        private final int kind;

        ShaderKind(int kind)
        {
            this.kind = kind;
        }
    }

    public static final class SPIRV implements NativeResource
    {
        private final long handle;
        private final boolean hasHandle;
        private ByteBuffer bytecode;

        public SPIRV(long handle, ByteBuffer bytecode)
        {
            this.handle = handle;
            this.bytecode = bytecode;
            this.hasHandle = true;
        }

        public SPIRV(ByteBuffer bytecode)
        {
            this.hasHandle = false;
            this.handle = 0;
            this.bytecode = bytecode;
        }

        public ByteBuffer bytecode()
        {
            return bytecode;
        }

        @Override
        public void free()
        {
            if (hasHandle)
            {
                shaderc_result_release(handle);
            }
            bytecode = null; // Help the GC
        }
    }

}