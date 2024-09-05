#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec3 inColor;
layout (location = 2) in vec2 inUv;

layout (location = 0) out vec3 fragColor;
layout (location = 1) out vec2 uv;

layout(set = 0, binding = 0) uniform GlobalUbo {
    mat4 projection;
    mat4 view;
    mat4 transformation[4];
} ubo;

layout(push_constant) uniform Push {
    mat4 transformation;
    vec4 color;
    int arrayIndex;
} push;

void main() {
//    gl_Position = ubo.projection * ubo.view * push.transformation * ubo.transformation[push.arrayIndex] * vec4(inPosition, 1.0);
    gl_Position = ubo.projection * ubo.view * ubo.transformation[push.arrayIndex] * push.transformation * vec4(inPosition, 1.0);
//    gl_Position.y += push.arrayIndex * 0.2f;
    fragColor = inColor;
    uv = inUv;
}