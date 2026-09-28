package com.jones.watermelonmod.client.fft;

/**
 * Resolution of the GPU FFT. LOW transforms a quarter of the pixels, which is
 * roughly four times cheaper and keeps laptop GPUs at a playable frame rate.
 */
public enum FftQuality {
    HIGH(10, 9),
    LOW(9, 8);

    private final int log2Width;
    private final int log2Height;

    FftQuality(int log2Width, int log2Height) {
        this.log2Width = log2Width;
        this.log2Height = log2Height;
    }

    public int log2Width() {
        return log2Width;
    }

    public int log2Height() {
        return log2Height;
    }

    public int width() {
        return 1 << log2Width;
    }

    public int height() {
        return 1 << log2Height;
    }
}
