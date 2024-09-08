#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 fragColor;
layout (location = 1) in vec2 uv;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

layout(push_constant) uniform Push {
    mat4 transformation;
    vec4 color;
    int arrayIndex;
} push;

void main() {
    outColor = texture(texSampler, uv);
    if (outColor.a == 0) discard;
}