#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // x = cutoff (0.01 to 1.0)
out vec4 fragColor;

void main() {
    vec2 frequency = (gl_FragCoord.xy - 0.5 * OutSize) / OutSize;
    float radius = length(frequency);
    float cutoff = max(Params.x, 0.01);
    float response = exp(-0.5 * pow(radius / cutoff, 2.0));
    fragColor = texelFetch(InSampler, ivec2(gl_FragCoord.xy), 0) * response;
}
