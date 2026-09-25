#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; }; // x = texture index (noise salt)
layout(std140) uniform VeilConfig { vec4 Kernel; vec4 Restore; }; // Kernel.w = noise sigma
out vec4 fragColor;

const float PI = 3.141592653589793;
// Blur sizes are given in reference pixels (a 1024-wide FFT); scale them to
// this FFT so the blur looks the same at every quality level.
float referenceScale() { return OutSize.x / 1024.0; }

float sinc(float x) {
    return abs(x) < 1.0e-4 ? 1.0 : sin(PI * x) / (PI * x);
}

// Bessel J1 (Numerical Recipes rational approximation); used for defocus blur.
float besselJ1(float x) {
    float ax = abs(x);
    if (ax < 8.0) {
        float y = x * x;
        float a = x * (72362614232.0 + y * (-7895059235.0 + y * (242396853.1 + y * (-2972611.439 + y * (15704.48260 + y * (-30.16036606))))));
        float b = 144725228442.0 + y * (2300535178.0 + y * (18583304.74 + y * (99447.43394 + y * (376.9991397 + y))));
        return a / b;
    }
    float z = 8.0 / ax;
    float y = z * z;
    float xx = ax - 2.356194491;
    float a = 1.0 + y * (0.183105e-2 + y * (-0.3516396496e-4 + y * (0.2457520174e-5 + y * (-0.240337019e-6))));
    float b = 0.04687499995 + y * (-0.2002690873e-3 + y * (0.8449199096e-5 + y * (-0.88228987e-6 + y * 0.105787412e-6)));
    float value = sqrt(0.636619772 / ax) * (cos(xx) * a - z * sin(xx) * b);
    return x < 0.0 ? -value : value;
}

// Real, centred blur responses H(u, v); f is in cycles per FFT pixel and H(0) = 1.
// kernel: x = type (0 Gaussian, 1 motion, 2 defocus), y = size, z = angle (radians).
float blurResponse(vec4 kernel, vec2 f) {
    int type = int(kernel.x + 0.5);
    float size = max(kernel.y, 0.0) * referenceScale();
    if (type == 0) return exp(-2.0 * PI * PI * size * size * dot(f, f));
    if (type == 1) return sinc(size * dot(f, vec2(cos(kernel.z), sin(kernel.z))));
    float r = PI * size * length(f);
    return r < 1.0e-4 ? 1.0 : 2.0 * besselJ1(r) / r;
}

// Centred frequency of this texel; the DC term sits exactly at (0, 0).
vec2 texelFrequency() {
    return (floor(gl_FragCoord.xy) - 0.5 * OutSize) / OutSize;
}

uint hash(uint x) {
    x ^= x >> 16; x *= 0x7feb352du;
    x ^= x >> 15; x *= 0x846ca68bu;
    x ^= x >> 16;
    return x;
}

float uniformNoise(uvec2 bin, uint salt) {
    return float(hash(bin.x * 1973u ^ hash(bin.y * 9277u ^ salt)) >> 8) / 16777216.0;
}

// Two independent standard normal values (Box-Muller). The seed depends only on
// the bin, so the noise is a fixed "dirty lens" pattern that never flickers.
vec2 gaussianNoise(uvec2 bin, uint salt) {
    float u1 = max(uniformNoise(bin, salt), 1.0e-7);
    float u2 = uniformNoise(bin, salt + 101u);
    return sqrt(-2.0 * log(u1)) * vec2(cos(2.0 * PI * u2), sin(2.0 * PI * u2));
}

// Degradation model G = H * F + N, applied to two packed complex spectra.
void main() {
    uvec2 bin = uvec2(gl_FragCoord.xy);
    uint salt = uint(Params.x) * 7919u + 17u;
    // Spatial noise sigma becomes sigma * sqrt(MN) per component of an unnormalised DFT bin.
    float binSigma = Kernel.w * sqrt(OutSize.x * OutSize.y);
    vec4 noise = binSigma * vec4(gaussianNoise(bin, salt), gaussianNoise(bin, salt + 31u));
    vec4 spectrum = texelFetch(InSampler, ivec2(gl_FragCoord.xy), 0);
    fragColor = blurResponse(Kernel, texelFrequency()) * spectrum + noise;
}
