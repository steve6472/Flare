#version 460
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec3 inColor;
layout (location = 2) in vec2 inUv;

layout (location = 0) out vec3 fragColor;
layout (location = 1) out vec2 uv;

layout(set = 0, binding = 0) uniform GlobalUbo {
    mat4 projection;
    mat4 view;
} ubo;

layout(std140, set = 0, binding = 1) readonly buffer Bones {
    mat4 transformation[4];
} bones;

void main() {
//    gl_Position = ubo.projection * ubo.view * push.transformation * ubo.transformation[push.arrayIndex] * vec4(inPosition, 1.0);
    gl_Position = ubo.projection * ubo.view * bones.transformation[gl_InstanceIndex] * vec4(inPosition, 1.0);
//    gl_Position = ubo.projection * ubo.view * vec4(inPosition, 1.0);
    fragColor = inColor;
    uv = inUv;
}