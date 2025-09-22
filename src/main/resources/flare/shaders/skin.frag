#version 460
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 normal;

layout (location = 0) out vec4 outColor;

layout (set = 0, binding = 2) uniform sampler2D texSampler;

#define MINECRAFT_LIGHT_POWER   (0.6)
#define MINECRAFT_AMBIENT_LIGHT (0.4)
const vec3 diffuseLight0 = normalize(vec3(0.2F, 1.0F, -0.7F));
const vec3 diffuseLight1 = normalize(vec3(-0.2F, 1.0F, 0.7F));

vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
    float light0 = max(0.0, dot(lightDir0, normal));
    float light1 = max(0.0, dot(lightDir1, normal));
    float lightAccum = min(1.0, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT);
    return vec4(color.rgb * lightAccum, color.a);
}

void main() {
    outColor = texture(texSampler, uv);
    if (outColor.a == 0) discard;

    outColor = minecraft_mix_light(diffuseLight0, diffuseLight1, normal, outColor);
}