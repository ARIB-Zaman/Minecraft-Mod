#version 330

uniform sampler2D InSampler;
in vec2 texCoord;
layout(std140) uniform SamplerInfo { vec2 OutSize; vec2 InSize; };
layout(std140) uniform FftConfig { vec4 Params; };
// Kernel = the wearer's estimate of the blur; Restore: x = mode (1 inverse,
// 2 pseudo-inverse, 3 Wiener), y = pseudo-inverse threshold, z = Wiener K.
layout(std140) uniform VeilConfig { vec4 Kernel; vec4 Restore; };
out vec4 fragColor;

// Caps the pure inverse filter so a zero of H can never produce inf/NaN,
// which would spread through the inverse FFT to every pixel on screen.
const float MAX_INVERSE_GAIN = 1000.0;

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

void main() {
    float h = blurResponse(Kernel, texelFrequency());
    int mode = int(Restore.x + 0.5);
    float gain = 1.0;
    if (mode == 1) {
        gain = sign(h) * min(1.0 / max(abs(h), 1.0e-6), MAX_INVERSE_GAIN);
    } else if (mode == 2) {
        gain = abs(h) > Restore.y ? 1.0 / h : 0.0;
    } else if (mode == 3) {
        gain = h / (h * h + max(Restore.z, 1.0e-6));
    }
    // Every H here is real, so the gain scales both packed complex values alike.
    fragColor = gain * texelFetch(InSampler, ivec2(gl_FragCoord.xy), 0);
}
