#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // x = low cutoff, y = high cutoff
out vec4 fragColor;

void main() {
    vec2 frequency = (gl_FragCoord.xy - 0.5 * OutSize) / OutSize;
    float radius = length(frequency);
    float hardMask = radius >= Params.x && radius <= Params.y ? 1.0 : 0.0;
    fragColor = texelFetch(InSampler, ivec2(gl_FragCoord.xy), 0) * hardMask;
}
