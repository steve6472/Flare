#version 460
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec2 uv;

layout (location = 0) out vec4 outColor;

layout (set = 0, binding = 2) uniform sampler2D texSampler;

void main() {
    outColor = texture(texSampler, uv);
    if (outColor.a == 0) discard;
}