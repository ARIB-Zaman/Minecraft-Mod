#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform SharpenConfig {
    float K00; float K01; float K02;
    float K10; float K11; float K12;
    float K20; float K21; float K22;
};

out vec4 fragColor;

vec3 sampleColor(vec2 offset) {
    return texture(InSampler, texCoord + offset / InSize).rgb;
}

void main() {
    // Each vec3 multiplication applies the same editable kernel coefficient
    // independently to red, green, and blue rather than collapsing to luma.
    vec3 sharpened = K00 * sampleColor(vec2(-1.0, -1.0)) + K01 * sampleColor(vec2(0.0, -1.0)) + K02 * sampleColor(vec2(1.0, -1.0))
                   + K10 * sampleColor(vec2(-1.0,  0.0)) + K11 * sampleColor(vec2(0.0,  0.0)) + K12 * sampleColor(vec2(1.0,  0.0))
                   + K20 * sampleColor(vec2(-1.0,  1.0)) + K21 * sampleColor(vec2(0.0,  1.0)) + K22 * sampleColor(vec2(1.0,  1.0));
    fragColor = vec4(clamp(sharpened, 0.0, 1.0), 1.0);
}
