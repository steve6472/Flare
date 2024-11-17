#version 450
#extension GL_ARB_separate_shader_objects: enable
#extension GL_EXT_nonuniform_qualifier : require

layout (location = 0) in vec2 uv;
layout (location = 1) flat in int index;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler[];
#define image texSampler[style.fontIndex]

struct FontStyle
{
    vec4 color;
    vec4 outlineColor;
    vec4 shadowColor;

    float softness;
    float outlineSoftness;
    float shadowSoftness;
    int soft; // 1 - true, 0 - false

    float thickness;
    float outlineThickness;
    float shadowThickness;
    int fontIndex;

    vec2 shadowOffset;
    vec2 atlasSize;
};

layout(std140, set = 0, binding = 2) readonly buffer FontSBO {
    FontStyle array[];
} styles;

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

float median(float a, float b, float c)
{
    return max(min(a, b), min(max(a, b), c));
}

float median(float a, float b, float c, float d)
{
    float maxAB = max(a, b);
    float minAB = min(a, b);
    float maxCD = max(c, d);
    float minCD = min(c, d);

    float middleMax = min(maxAB, maxCD);
    float middleMin = max(minAB, minCD);

    return (middleMax + middleMin) * 0.5;
}

void main()
{
    FontStyle style = styles.array[index];

    float sdf = 0;
    if (style.soft == 1)
    {
        sdf = texture(image, uv).a;
    } else
    {
        vec4 msd = texture(image, uv);
        sdf = median(msd.r, msd.g, msd.b);
    }
    float distance = smoothstep(1.0 - style.thickness - style.softness, 1.0 - style.thickness + style.softness, sdf);

    float outline = smoothstep(style.outlineThickness - style.outlineSoftness, style.outlineThickness + style.outlineSoftness, sdf);
    vec4 mainImage = vec4(mix(style.outlineColor.rgb, style.color.rgb, outline), clamp(distance, 0, 1));

    float shadow = texture(image, uv - style.shadowOffset / style.atlasSize).a;
    shadow = smoothstep(1.0 - style.shadowThickness - style.shadowSoftness, 1.0 - style.shadowThickness + style.shadowSoftness, shadow);
    vec4 shadowImage = vec4(style.shadowColor.rgb, clamp(shadow, 0, 1));

    vec4 finalImage = blendImages(shadowImage, mainImage);

    if (finalImage.a <= 0)
//        outColor = vec4(1, 0, 1, 1);
        discard;
    else
        outColor = finalImage;

//    outColor = finalImage;
}