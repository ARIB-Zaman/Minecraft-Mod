#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform EdgeConfig {
    float K00; float K01; float K02;
    float K10; float K11; float K12;
    float K20; float K21; float K22;
};

out vec4 fragColor;

float luminanceAt(vec2 offset) {
    return dot(texture(InSampler, texCoord + offset / InSize).rgb, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    float tl = luminanceAt(vec2(-1.0, -1.0));
    float  t = luminanceAt(vec2( 0.0, -1.0));
    float tr = luminanceAt(vec2( 1.0, -1.0));
    float  l = luminanceAt(vec2(-1.0,  0.0));
    float  r = luminanceAt(vec2( 1.0,  0.0));
    float bl = luminanceAt(vec2(-1.0,  1.0));
    float  b = luminanceAt(vec2( 0.0,  1.0));
    float br = luminanceAt(vec2( 1.0,  1.0));

    // The coefficients come directly from the editable workbench matrix.
    float convolution = K00 * tl + K01 * t + K02 * tr
                      + K10 * l  + K11 * luminanceAt(vec2(0.0)) + K12 * r
                      + K20 * bl + K21 * b + K22 * br;
    float edge = clamp(abs(convolution) * 1.5, 0.0, 1.0);
    fragColor = vec4(vec3(edge), 1.0);
}
