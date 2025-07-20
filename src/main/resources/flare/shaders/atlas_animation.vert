#version 450
#extension GL_ARB_separate_shader_objects: enable

layout (location = 0) in vec3 inPosition;
layout (location = 1) in vec3 inData;

layout (location = 0) out vec2 uv;
layout (location = 1) out vec3 data;

layout(set = 0, binding = 0) uniform GlobalUbo {
    mat4 projection;
    mat4 view;
} camera;

const vec2 uvCoords[6] = vec2[](
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0),
    vec2(0.0, 0.0)
);

void main() {
    gl_Position = camera.projection * camera.view * vec4(inPosition, 1.0);
    uv = uvCoords[gl_VertexIndex % 6];
    data = inData;
}