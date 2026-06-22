package com.geoforge.engine.noise;

/**
 * Per-thread cache for chunk generation data. Accessed exclusively via {@link
 * java.lang.ThreadLocal}.
 *
 * <p>Mutable by design — each thread gets its own instance. The {@code heightmap} array
 * stores local chunk column heights for a 16×16 chunk area.
 */
public final class NoiseCache {

    private static final int CHUNK_SIZE = 16;

    private final float[] heightmap = new float[CHUNK_SIZE * CHUNK_SIZE];

    /** Returns the heightmap array (mutable, owned by the caller's thread). */
    public float[] heightmap() {
        return heightmap;
    }

    /** Resets all height values to zero. */
    public void reset() {
        java.util.Arrays.fill(heightmap, 0.0f);
    }
}
