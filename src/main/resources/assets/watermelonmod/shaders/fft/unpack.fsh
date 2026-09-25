#version 330

uniform sampler2D RGSampler;
uniform sampler2D BSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; };
out vec4 fragColor;

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    vec4 rg = texelFetch(RGSampler, pixel, 0);
    float blue = texelFetch(BSampler, pixel, 0).r;
    float phase = ((pixel.x + pixel.y) & 1) == 0 ? 1.0 : -1.0;
    vec3 rgb = phase * vec3(rg.r, rg.b, blue) / (OutSize.x * OutSize.y);
    fragColor = vec4(clamp(rgb, 0.0, 1.0), 1.0);
}
