#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec2 inUv;
layout (location = 2) in vec3 inData;

layout (location = 0) out vec2 uv;
layout (location = 1) out vec3 data;

layout(set = 0, binding = 0) uniform GlobalUbo {
    mat4 projection;
    mat4 view;
} ubo;

void main() {
    gl_Position = ubo.projection * ubo.view * vec4(inPosition, 1.0);
    uv = inUv;
    data = inData;
}