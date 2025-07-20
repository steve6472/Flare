#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec2 uv;
layout (location = 1) flat in vec3 data;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

struct AnimationData
{
    // Dimensions in UV coordinates of atlas
    vec4 dimensions;

    // Size of single sprite
    vec2 singleSize;
    int indexFrom;
    int indexTo;

    float transition;
    int flags;
    vec2 pixelScale;
};

layout(std140, set = 0, binding = 2) readonly buffer AnimDataPlural
{
    AnimationData array[];
} animationData;

bool isInterpolation(int flags) { return (flags & 0x1) == 1; }
bool isOkLab(int flags)  { return ((flags >> 1) & 0x01) == 1; }

float map(float value, float inMin, float inMax, float outMin, float outMax)
{
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

// https://www.shadertoy.com/view/ttcyRS
vec3 oklab_mix( vec3 colA, vec3 colB, float h )
{
    // https://bottosson.github.io/posts/oklab
    const mat3 kCONEtoLMS = mat3(
    0.4122214708,  0.2119034982,  0.0883024619,
    0.5363325363,  0.6806995451,  0.2817188376,
    0.0514459929,  0.1073969566,  0.6299787005);
    const mat3 kLMStoCONE = mat3(
     4.0767416621,  -1.2684380046, -0.0041960863,
    -3.3077115913,   2.6097574011, -0.7034186147,
     0.2309699292,  -0.3413193965,  1.7076147010);

    // rgb to cone (arg of pow can't be negative)
    vec3 lmsA = pow(kCONEtoLMS * colA, vec3(1.0 / 3.0));
    vec3 lmsB = pow(kCONEtoLMS * colB, vec3(1.0 / 3.0));
    // lerp
    vec3 lms = mix(lmsA, lmsB, h);
    // gain in the middle (no oaklab anymore, but looks better?)
    // lms *= 1.0+0.2*h*(1.0-h);
    // cone to rgb
    return kLMStoCONE * (lms * lms * lms);
}

void main()
{
    AnimationData data = animationData.array[int(data.x)];
    vec2 scale = data.pixelScale / data.singleSize;

    if (isInterpolation(data.flags))
    {
        vec2 indiciesFrom = vec2(data.indexFrom % int(scale.x), data.indexFrom / int(scale.x));
        vec2 indiciesTo = vec2(data.indexTo % int(scale.x), data.indexTo / int(scale.x));

        vec2 uvFrom = vec2(uv);
        uvFrom.x = map(uvFrom.x, -indiciesFrom.x, scale.x - indiciesFrom.x, data.dimensions.x, data.dimensions.z);
        uvFrom.y = map(uvFrom.y, -indiciesFrom.y, scale.y - indiciesFrom.y, data.dimensions.y, data.dimensions.w);

        vec2 uvTo = vec2(uv);
        uvTo.x = map(uvTo.x, -indiciesTo.x, scale.x - indiciesTo.x, data.dimensions.x, data.dimensions.z);
        uvTo.y = map(uvTo.y, -indiciesTo.y, scale.y - indiciesTo.y, data.dimensions.y, data.dimensions.w);

        vec4 colFrom = texture(texSampler, uvFrom);
        vec4 colTo = texture(texSampler, uvTo);

        if (isOkLab(data.flags))
        {
            outColor = vec4(oklab_mix(colFrom.rgb, colTo.rgb, data.transition), mix(colFrom.a, colTo.a, data.transition));
        }
        else
        {
            outColor = mix(colFrom, colTo, data.transition);
        }
    } else
    {
        vec2 indicies = vec2(data.indexFrom % int(scale.x), data.indexFrom / int(scale.x));

        vec2 theUv = vec2(uv);
        theUv.x = map(theUv.x, -indicies.x, scale.x - indicies.x, data.dimensions.x, data.dimensions.z);
        theUv.y = map(theUv.y, -indicies.y, scale.y - indicies.y, data.dimensions.y, data.dimensions.w);

        outColor = texture(texSampler, theUv);
    }
}