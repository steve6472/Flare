package steve6472.flare.pipeline.shader;

import steve6472.core.util.Preconditions;
import steve6472.flare.ShaderSPIRVUtils;

/**
 * Created by steve6472
 * Date: 11/8/2025
 * Project: Flare <br>
 */
public record ShaderId(String file, ShaderSPIRVUtils.ShaderKind kind, int stage, String entryPoint)
{
    public static final String DEFAULT_ENTRY_POINT = "main";

    public ShaderId(String file, ShaderSPIRVUtils.ShaderKind kind, int stage)
    {
        this(file, kind, stage, DEFAULT_ENTRY_POINT);
    }

    public ShaderId
    {
        Preconditions.checkNotNull(kind, "Shader kind has to be selected");
        Preconditions.checkNotNull(file, "Shader file has to be selected");
        Preconditions.checkNotNull(entryPoint, "Shader entry point has to be selected");
    }
}
