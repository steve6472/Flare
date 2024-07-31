#version 450
#extension GL_ARB_separate_shader_objects: enable

//layout (location = 0) in vec3 fragColor;

layout (location = 0) out vec4 outColor;

layout(push_constant) uniform Push {
    vec2 offset;
    vec3 color;
} push;

void main() {
//    outColor = vec4(fragColor, 1.0);
    outColor = vec4(push.color, 1.0);
}