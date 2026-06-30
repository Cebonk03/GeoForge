package com.geoforge.engine.noise;

/**
 * Deprecated subclass of {@link GradientNoise} kept for backward compatibility.
 *
 * <p>This class exists solely to preserve binary compatibility with code compiled
 * against the old class name "SimplexNoise". All new code should use
 * {@link GradientNoise} directly.
 *
 * @deprecated Use {@link GradientNoise} directly. Scheduled for removal in a future release.
 */
@Deprecated
public final class SimplexNoise extends GradientNoise {

    /**
     * Creates a simplex noise instance (delegates to {@link GradientNoise}).
     *
     * @param seed the seed for deterministic noise generation
     */
    public SimplexNoise(long seed) {
        super(seed);
    }
}
