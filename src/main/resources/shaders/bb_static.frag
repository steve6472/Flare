#version 450
#extension GL_ARB_separate_shader_objects: enable

#define AMBIENT 0.5
#define XFAC -0.15
#define ZFAC 0.05

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 normal;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

layout(push_constant) uniform Push {
    mat4 transformation;
    vec4 color;
    int arrayIndex;
} push;




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
    vec4 textureColor = texture(texSampler, uv);
    if (textureColor.a == 0) discard;

//    float yLight = (1.0 + normal.y) * 0.5;
//    float light = yLight * (1.0 - AMBIENT) + normal.x * normal.x * XFAC + normal.z * normal.z * ZFAC + AMBIENT;

//    textureColor.rgb *= light;

    outColor = minecraft_mix_light(diffuseLight0, diffuseLight1, normal, textureColor);
//    outColor = vec4(normal, 1.0);
}