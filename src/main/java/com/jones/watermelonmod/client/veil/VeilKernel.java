package com.jones.watermelonmod.client.veil;

/**
 * One blur model: a point-spread function plus additive noise. Sizes are in
 * reference pixels (a 1024-wide FFT), so a kernel looks the same at every
 * FFT quality level.
 */
public record VeilKernel(Type type, float size, float angleDegrees, float noiseSigma) {
    public enum Type {
        GAUSSIAN,
        MOTION,
        DEFOCUS;

        public static Type fromIndex(int index) {
            Type[] types = values();
            return types[Math.clamp(index, 0, types.length - 1)];
        }
    }

    /** Layout of the shaders' {@code VeilConfig.Kernel}: type, size, angle (radians), noise sigma. */
    public float[] shaderKernel() {
        return new float[]{type.ordinal(), size, (float) Math.toRadians(angleDegrees), noiseSigma};
    }
}
