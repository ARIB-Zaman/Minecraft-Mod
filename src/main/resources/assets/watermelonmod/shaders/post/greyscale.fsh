#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform GreyscaleConfig {
    float Intensity;
};

out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float luminance = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    fragColor = vec4(mix(color.rgb, vec3(luminance), clamp(Intensity, 0.0, 1.0)), color.a);
}
