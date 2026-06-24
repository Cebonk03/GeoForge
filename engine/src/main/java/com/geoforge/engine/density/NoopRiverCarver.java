package com.geoforge.engine.density;

/**
 * A {@link RiverCarver} that performs no carving.
 *
 * <p>This is the default implementation, returning the density unchanged.
 * It acts as a safe default until a real carver implementation is wired in.
 */
public final class NoopRiverCarver implements RiverCarver {

    private static final NoopRiverCarver INSTANCE = new NoopRiverCarver();

    private NoopRiverCarver() {}

    /**
     * Returns the singleton instance.
     *
     * @return the shared NoopRiverCarver instance
     */
    public static NoopRiverCarver instance() {
        return INSTANCE;
    }

    @Override
    public double carve(double density, int blockX, int blockY, int blockZ) {
        return density;
    }
}
