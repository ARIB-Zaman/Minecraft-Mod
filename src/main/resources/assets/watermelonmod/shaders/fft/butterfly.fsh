#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // stage, vertical, inverse, unused
out vec4 fragColor;
const float PI = 3.141592653589793;

vec2 multiplyComplex(vec2 a, vec2 b) { return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x); }

void main() {
    ivec2 pixel = ivec2(gl_FragCoord.xy);
    int stage = int(Params.x);
    bool vertical = Params.y > 0.5;
    int index = vertical ? pixel.y : pixel.x;
    int groupSize = 1 << (stage + 1);
    int halfGroup = groupSize >> 1;
    int position = index % groupSize;
    int base = index - position;
    int offset = position % halfGroup;
    int aIndex = base + offset;
    int bIndex = aIndex + halfGroup;
    ivec2 aCoord = vertical ? ivec2(pixel.x, aIndex) : ivec2(aIndex, pixel.y);
    ivec2 bCoord = vertical ? ivec2(pixel.x, bIndex) : ivec2(bIndex, pixel.y);
    vec4 a = texelFetch(InSampler, aCoord, 0);
    vec4 b = texelFetch(InSampler, bCoord, 0);
    float sign = Params.z > 0.5 ? 1.0 : -1.0;
    float angle = sign * 2.0 * PI * float(offset) / float(groupSize);
    vec2 twiddle = vec2(cos(angle), sin(angle));
    vec4 weightedB = vec4(multiplyComplex(b.rg, twiddle), multiplyComplex(b.ba, twiddle));
    fragColor = position < halfGroup ? a + weightedB : a - weightedB;
}
