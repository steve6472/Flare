#version 450
#extension GL_ARB_separate_shader_objects: enable

/*
 * Debug settings
 */

// Line color mode
#define HALVE 0
#define INVERT 1
#define INVERT_HALVE 2

#define PIXEL_GRID false
#define LINE_WIDTH 0.018
#define MODE HALVE

// Render mode
#define TEXTURE 0
#define UV 1

#define RENDER_MODE TEXTURE

layout (location = 0) in vec2 uv;
layout (location = 1) in vec3 color;
layout (location = 2) flat in vec3 data;

layout (location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D texSampler;

struct UITexture
{
    // Dimensions in UV coordinates of atlas
    vec4 dimensions;

    // Border in pixels
    vec4 border;

    int flags;
    int _align0;
    vec2 pixelScale;
};

layout(std140, set = 0, binding = 2) readonly buffer UITextureSettings
{
    UITexture array[];
} textures;

layout(push_constant) uniform Push
{
    // Pixel width and height of the atlas
    float textureWidth;
    float textureHeight;
} push;

float map(float value, float inMin, float inMax, float outMin, float outMax)
{
    return outMin + (outMax - outMin) * (value - inMin) / (inMax - inMin);
}

bool isStretchInner(int flags)
{
    return ((flags >> 2) & 0x01) == 1;
}

int getTextureType(int flags)
{
    return flags & 0x3;
}

vec2 calculateNineSliceUV(UITexture uiTexture)
{
    vec2 spritePixelSize = data.yz;
    vec2 texturePixelSize = uiTexture.pixelScale;
    /*
     * Values for border checking
     */
    vec4 border = uiTexture.border;
    //                         x=20.0 / 40.0   |   y=20.0 / 20.0
    vec2 _borderScaleFactor = texturePixelSize / spritePixelSize;
    vec4 _borderScaled = border * _borderScaleFactor.xyxy;
    float borderLeft = _borderScaled.x;
    float borderTop = _borderScaled.y;
    float borderRight = texturePixelSize.x - _borderScaled.z;
    float borderBottom = texturePixelSize.y - _borderScaled.w;
    // 0-1 to 20-20
    vec2 texturePixelUV = uv * texturePixelSize;

    vec2 spriteUV = vec2(uv);
    float spriteLeft = border.x / spritePixelSize.x;
    float spriteTop = border.y / spritePixelSize.y;
    float spriteRight = (spritePixelSize.x - border.z) / spritePixelSize.x;
    float spriteBottom = (spritePixelSize.y - border.w) / spritePixelSize.y;

    float textureLeft = border.x / texturePixelSize.x;
    float textureTop = border.y / texturePixelSize.y;
    float textureRight = (texturePixelSize.x - border.z) / texturePixelSize.x;
    float textureBottom = (texturePixelSize.y - border.w) / texturePixelSize.y;

    vec2 mappedSides = vec2(spriteUV);
    mappedSides.x = map(mappedSides.x, spriteLeft, spriteRight, 0.0, 1.0);
    mappedSides.x *= (spritePixelSize.x - border.x - border.z) / (texturePixelSize.x - border.x - border.z);
    mappedSides.x = mod(mappedSides.x, 1.0);
    mappedSides.x = map(mappedSides.x, 0.0, 1.0, textureLeft, textureRight);

    mappedSides.y = map(mappedSides.y, spriteTop, spriteBottom, 0.0, 1.0);
    mappedSides.y *= (spritePixelSize.y - border.y - border.w) / (texturePixelSize.y - border.y - border.w);
    mappedSides.y = mod(mappedSides.y, 1.0);
    mappedSides.y = map(mappedSides.y, 0.0, 1.0, textureTop, textureBottom);

    // TOP
    if (texturePixelUV.y < borderTop)
    {
        // Left
        if (texturePixelUV.x < borderLeft)
        {
            spriteUV.x *= spritePixelSize.x / texturePixelSize.x;
            spriteUV.y *= spritePixelSize.y / texturePixelSize.y;
        }
        // Middle
        else if (texturePixelUV.x > borderLeft && texturePixelUV.x < borderRight)
        {
            spriteUV.x = mappedSides.x;
            spriteUV.y *= spritePixelSize.y / texturePixelSize.y;
        }
        // Right
        else if (texturePixelUV.x > borderRight)
        {
            spriteUV.x = map(spriteUV.x, spriteRight, 1.0, textureRight, 1.0);
            spriteUV.y *= spritePixelSize.y / texturePixelSize.y;
        }
    }
    // MIDDLE
    else if (texturePixelUV.y > borderTop && texturePixelUV.y < borderBottom)
    {
        // Left
        if (texturePixelUV.x < borderLeft)
        {
            spriteUV.x *= spritePixelSize.x / texturePixelSize.x;

            spriteUV.y = mappedSides.y;
        }
        // Middle
        else if (texturePixelUV.x > borderLeft && texturePixelUV.x < borderRight)
        {
            if (isStretchInner(uiTexture.flags))
            {
                spriteUV.x = map(spriteUV.x, spriteLeft, spriteRight, textureLeft, textureRight);
                spriteUV.y = map(spriteUV.y, spriteTop, spriteBottom, textureTop, textureBottom);
            } else
            {
                spriteUV = mappedSides;
            }
        }
        // Right
        else if (texturePixelUV.x > borderRight)
        {
            spriteUV.x = map(spriteUV.x, spriteRight, 1.0, textureRight, 1.0);
            spriteUV.y = mappedSides.y;
        }
    }
    // BOTTOM
    else if (texturePixelUV.y > borderBottom)
    {
        // Left
        if (texturePixelUV.x < borderLeft)
        {
            spriteUV.x *= spritePixelSize.x / texturePixelSize.x;
            spriteUV.y = map(spriteUV.y, spriteBottom, 1.0, textureBottom, 1.0);
        }
        // Middle
        else if (texturePixelUV.x > borderLeft && texturePixelUV.x < borderRight)
        {
            spriteUV.x = mappedSides.x;
            spriteUV.y = map(spriteUV.y, spriteBottom, 1.0, textureBottom, 1.0);
        }
        // Right
        else if (texturePixelUV.x > borderRight)
        {
            spriteUV.x = map(spriteUV.x, spriteRight, 1.0, textureRight, 1.0);
            spriteUV.y = map(spriteUV.y, spriteBottom, 1.0, textureBottom, 1.0);
        }
    }

    return spriteUV;
}

void main()
{
    UITexture uiTexture = textures.array[int(data.x)];
    int textureType = getTextureType(uiTexture.flags);
    vec2 spritePixelSize = data.yz;
    vec2 texturePixelSize = uiTexture.pixelScale;

    // STRETCH
    if (textureType == 0)
    {
        vec2 spriteUV = vec2(uv);
        spriteUV.x = map(spriteUV.x, 0.0, 1.0, uiTexture.dimensions.x, uiTexture.dimensions.z);
        spriteUV.y = map(spriteUV.y, 0.0, 1.0, uiTexture.dimensions.y, uiTexture.dimensions.w);
        vec4 col = texture(texSampler, spriteUV);
        if (col.a == 0)
            discard;
        outColor = col;
    }
    // NINE_SLICE
    else if (textureType == 1)
    {
        vec2 spriteUV = calculateNineSliceUV(uiTexture);

        if (RENDER_MODE == TEXTURE)
        {
            spriteUV.x = map(spriteUV.x, 0.0, 1.0, uiTexture.dimensions.x, uiTexture.dimensions.z);
            spriteUV.y = map(spriteUV.y, 0.0, 1.0, uiTexture.dimensions.y, uiTexture.dimensions.w);
        }
        vec4 col = texture(texSampler, spriteUV);

        if (RENDER_MODE == UV)
        {
            col = vec4(spriteUV.x, spriteUV.y, 0, 1.0);
        }

        if (PIXEL_GRID)
        {
            vec2 scaledUV = uv * spritePixelSize;
            if (mod(scaledUV.x, 1.0) < LINE_WIDTH || mod(scaledUV.y, 1.0) < LINE_WIDTH || mod(scaledUV.x, -1.0) > -LINE_WIDTH || mod(scaledUV.y, -1.0) > -LINE_WIDTH)
            {
                if (MODE == HALVE) col *= vec4(0.5, 0.5, 0.5, 1.0);
                if (MODE == INVERT) col = vec4(1.0 - col.x, 1.0 - col.y, 1.0 - col.z, col.w);
                if (MODE == INVERT_HALVE) col = vec4(1.0 - col.x, 1.0 - col.y, 1.0 - col.z, col.w) * vec4(0.5, 0.5, 0.5, 1.0);
            }
        }

        if (col.a == 0)
            discard;

        outColor = col;
    }
    // TILED
    else if (textureType == 2)
    {
        outColor = vec4(0, 1, 0, 1.0);
    }
    // UNUSED, DEBUG
    else
    {
        vec2 spriteUV = vec2(uv);
        spriteUV.x = map(spriteUV.x, 0.0, 1.0, uiTexture.dimensions.x, uiTexture.dimensions.z);
        spriteUV.y = map(spriteUV.y, 0.0, 1.0, uiTexture.dimensions.y, uiTexture.dimensions.w);
        outColor = vec4(uv.x, uv.y, 0, 1.0);
    }

    outColor *= vec4(color, 1.0);
}