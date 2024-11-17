#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec4 fragColor;
layout (location = 1) in vec2 uv;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

void main()
{
    vec4 col = texture(texSampler, uv);

//    if (col.r < 0.25)
//        discard;

    col.a = col.r;

    outColor = col * fragColor;
}