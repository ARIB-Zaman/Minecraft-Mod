#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // x = log2(width), y = log2(height)
out vec4 fragColor;

int bitReverse(int value, int bits) {
    int reversed = 0;
    for (int i = 0; i < 10; ++i) { if (i >= bits) break; reversed = (reversed << 1) | ((value >> i) & 1); }
    return reversed;
}

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    fragColor = texelFetch(InSampler, ivec2(bitReverse(pixel.x, int(Params.x)), bitReverse(pixel.y, int(Params.y))), 0);
}
