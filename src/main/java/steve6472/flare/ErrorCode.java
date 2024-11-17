package steve6472.flare;

public enum ErrorCode
{
    /*
     * Vulkan errors
     */
    GLFW_INIT("Cannot initialize GLFW"),
    WINDOW_CREATION("Cannot create window"),
    FAILED_TO_CREATE_VULKAN_INSTANCE("Failed to create instance"),
    VALIDATION_NOT_ENABLED("Validation layers are not enabled"),
    VALIDATION_NOT_SUPPORTED("Validation requested but not supported"),
    SETUP_DEBUG_MESSANGER("Failed to set up debug messenger"),
    NO_VULKAN_GPU("Failed to find GPUs with Vulkan support"),
    NO_SUITABLE_GPU("Failed to find a suitable GPU"),
    NO_LOGICAL_DEVICE("Failed to create logical device"),
    WINDOW_SURFACE_CREATION("Failed to create window surface"),
    SWAP_CHAIN_CREATION("Failed to create swap chain"),
    IMAGE_VIEWS_CREATION("Failed to create image views"),
    COMMAND_POOL_CREATION("Failed to create command pool"),
    COMMAND_BUFFER_ALLOCATION("Failed to allocate command buffers"),
    BEGIN_COMMAND_RECORDING("Failed to begin recording command buffer"),
    END_COMMAND_RECORDING("Failed to record command buffer"),
    VERTEX_BUFFER_CREATION("Failed to create vertex buffer"),
    VERTEX_BUFFER_ALLOCATION("Failed to allocate vertex buffer memory"),
    FIND_MEMORY_TYPE("Failed to find suitable memory type"),
    SUBMIT_COPY_COMMAND_BUFFER("Failed to submit copy command buffer"),
    CREATE_FRAMEBUFFER("Failed to create framebuffer"),
    PIPELINE_CREATION("Failed to create pipeline layout"),
    GRAPHICS_PIPELINE_CREATION("Failed to create graphics pipeline"),
    RENDER_PASS_CREATION("Failed to create render pass"),
    SHADER_MODULE_CREATION("Failed to create shader module"),

    REGISTRY_LOADING_ERROR("Error while loading registry %s")
    ;

    public final String description;

    ErrorCode(String description)
    {
        this.description = description;
    }

    ErrorCode()
    {
        this.description = name();
    }

    public int errorCode()
    {
        return ordinal() + 1;
    }

    public String format(Object... objs)
    {
        return errorCode() + " -> " + description.formatted(objs);
    }
}
