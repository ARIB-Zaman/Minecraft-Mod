#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; };
out vec4 fragColor;

void main() {
    fragColor = texture(InSampler, texCoord);
}
