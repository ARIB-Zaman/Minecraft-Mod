#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // x = opacity (1.0 = fully replace the scene; used for the success fade-out)
out vec4 fragColor;

void main() {
    fragColor = vec4(texture(InSampler, texCoord).rgb, clamp(Params.x, 0.0, 1.0));
}
