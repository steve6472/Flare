#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inUv;

layout (location = 0) out vec2 uv;
layout (location = 1) out vec3 normal;

layout(set = 0, binding = 0) uniform GlobalUbo {
    mat4 projection;
    mat4 view;
} ubo;

layout(std140, set = 0, binding = 2) readonly buffer SSBO {
    mat4 transformation[];
} ssbo;

layout(push_constant) uniform Push {
    int arrayIndex;
} push;

void main() {
    mat4 transform = ssbo.transformation[gl_InstanceIndex + push.arrayIndex];

    gl_Position = ubo.projection * ubo.view * transform * vec4(inPosition, 1.0);

    uv = inUv;
    normal = normalize(mat3(transpose(inverse(transform))) * inNormal);
}