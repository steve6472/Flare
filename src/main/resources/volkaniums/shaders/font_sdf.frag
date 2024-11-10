#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec4 fragColor;
layout (location = 1) in vec2 uv;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

const float softness = 0.0;
const float thickness = 0.7; // range 0..1

const vec4 outlineColor = vec4(1.0, 1.0, 1.0, 1);
const float outlineThickness = 0.5;
const float outlineSoftness = 0.5;

const vec2 shadowOffset = vec2(-0.003, -0.003);
const float shadowSoftness = 0.2;
const float shadowThickness = 0.7;
const vec4 shadowColor = vec4(0.07, 0.07, 0.07, 1.0);

vec4 blendImages(vec4 colorA, vec4 colorB)
{
    // Perform proper alpha blending (assumes both colors are premultiplied by their alphas)
    float alphaA = colorA.a;
    float alphaB = colorB.a;

    // Composite color using alpha blending
    vec3 blendedColor = (colorA.rgb * alphaA * (1.0 - alphaB)) + (colorB.rgb * alphaB);

    // Final alpha is the sum of individual alphas, clipped to [0, 1]
    float finalAlpha = alphaA + alphaB - (alphaA * alphaB);

    return vec4(blendedColor, finalAlpha);
}

void main()
{
    float sdf = texture(texSampler, uv).r;
    float distance = smoothstep(1.0 - thickness - softness, 1.0 - thickness + softness, sdf);

    float outline = smoothstep(outlineThickness - outlineSoftness, outlineThickness + outlineSoftness, sdf);
    vec4 mainImage = vec4(mix(outlineColor.rgb, fragColor.rgb, outline), clamp(distance, 0, 1));

    float shadow = texture(texSampler, uv + shadowOffset).r;
    shadow = smoothstep(1.0 - shadowThickness - shadowSoftness, 1.0 - shadowThickness + shadowSoftness, shadow);
    vec4 shadowImage = vec4(shadowColor.rgb, clamp(shadow, 0, 1));

    vec4 finalImage = blendImages(shadowImage, mainImage);

    if (finalImage.a <= 0)
        discard;

    outColor = finalImage;
}