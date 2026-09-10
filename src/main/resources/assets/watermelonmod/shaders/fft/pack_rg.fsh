#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; };
out vec4 fragColor;

int bitReverse(int value, int bits) {
    int reversed = 0;
    for (int i = 0; i < 10; ++i) {
        if (i >= bits) break;
        reversed = (reversed << 1) | ((value >> i) & 1);
    }
    return reversed;
}

void main() {
    ivec2 outputPixel = ivec2(gl_FragCoord.xy);
    ivec2 inputPixel = ivec2(bitReverse(outputPixel.x, 10), bitReverse(outputPixel.y, 9));
    vec3 color = texelFetch(InSampler, clamp(inputPixel * ivec2(InSize) / ivec2(OutSize), ivec2(0), ivec2(InSize) - 1), 0).rgb;
    float phase = ((inputPixel.x + inputPixel.y) & 1) == 0 ? 1.0 : -1.0;
    fragColor = vec4(color.r * phase, 0.0, color.g * phase, 0.0);
}
