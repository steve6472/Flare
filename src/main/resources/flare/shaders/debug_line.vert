#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec4 inColor;

layout (location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform GlobalUbo {
    mat4 projection;
    mat4 view;
} ubo;

void main() {
    gl_Position = ubo.projection * ubo.view * vec4(inPosition, 1.0);
    fragColor = inColor;
}