#version 330

uniform sampler2D RGSampler;
uniform sampler2D BSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; };
out vec4 fragColor;

void main() {
    // The forward FFT is already shifted by (-1)^(x+y), so DC is centred.
    vec4 rg = texture(RGSampler, texCoord);
    vec4 blue = texture(BSampler, texCoord);
    float redMagnitude = length(rg.rg);
    float greenMagnitude = length(rg.ba);
    float blueMagnitude = length(blue.rg);
    float magnitude = dot(vec3(redMagnitude, greenMagnitude, blueMagnitude), vec3(0.2126, 0.7152, 0.0722));
    // A forward FFT is unnormalised; scale by N, then strongly compress its
    // huge dynamic range. The gamma lift makes ordinary scene detail legible
    // while preserving the ordering of actual coefficient magnitudes.
    float normalisedMagnitude = magnitude / float(1024 * 512);
    const float displayGain = 65536.0;
    float logMagnitude = log(1.0 + displayGain * normalisedMagnitude) / log(1.0 + displayGain);
    float displayMagnitude = pow(clamp(logMagnitude, 0.0, 1.0), 0.45);
    fragColor = vec4(vec3(clamp(displayMagnitude, 0.0, 1.0)), 1.0);
}
