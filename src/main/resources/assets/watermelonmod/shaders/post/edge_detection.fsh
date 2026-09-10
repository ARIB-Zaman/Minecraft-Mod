#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform EdgeConfig {
    float Rotation;
};

out vec4 fragColor;

float luminanceAt(vec2 offset) {
    return dot(texture(InSampler, texCoord + offset / InSize).rgb, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    float tl = luminanceAt(vec2(-1.0, -1.0));
    float  t = luminanceAt(vec2( 0.0, -1.0));
    float tr = luminanceAt(vec2( 1.0, -1.0));
    float  l = luminanceAt(vec2(-1.0,  0.0));
    float  r = luminanceAt(vec2( 1.0,  0.0));
    float bl = luminanceAt(vec2(-1.0,  1.0));
    float  b = luminanceAt(vec2( 0.0,  1.0));
    float br = luminanceAt(vec2( 1.0,  1.0));

    // Sobel Gx. Rotating this kernel swaps it to Gy or reverses its sign.
    float gx = (tr + 2.0 * r + br) - (tl + 2.0 * l + bl);
    float gy = (bl + 2.0 * b + br) - (tl + 2.0 * t + tr);
    int rotation = int(floor(Rotation + 0.5)) % 4;
    float convolution = (rotation == 0 || rotation == 2) ? gx : gy;
    if (rotation == 2 || rotation == 3) convolution = -convolution;
    float edge = clamp(abs(convolution) * 1.5, 0.0, 1.0);
    fragColor = vec4(vec3(edge), 1.0);
}
