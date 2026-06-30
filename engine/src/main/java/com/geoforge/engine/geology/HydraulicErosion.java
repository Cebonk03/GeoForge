package com.geoforge.engine.geology;

import java.util.SplittableRandom;

/**
 * Hydraulic erosion simulation using a droplet-based approach.
 *
 * <p>Instances are configured with gravity and a maximum number of simulation steps per droplet.
 * The heightmap array is provided by the caller and modified in-place.
 *
 * <p><b>Limitation:</b> This implementation operates on a 2D heightmap extracted from the
 * 3D density field. It cannot erode overhangs or cave interiors. For volumetric
 * (3D) erosion, a future implementation would need to operate directly on the density
 * field rather than a pre-extracted heightmap.
 */
public final class HydraulicErosion {

    private static final float MIN_VELOCITY = 0.01f;
    private static final float EVAPORATION = 0.01f;
    private static final float SEDIMENT_CAPACITY = 8.0f;
    private static final float DEPOSITION = 0.5f;
    private static final float EROSION = 0.5f;

    private final int maxSteps;
    private final float gravity;

    /** Creates an erosion simulator with default gravity of 0.2 and 10 max steps. */
    public HydraulicErosion() {
        this(10, 0.2f);
    }

    /**
     * Creates an erosion simulator with a configurable maximum step count per droplet.
     * Uses default gravity of 0.2.
     *
     * @param maxSteps the maximum number of simulation steps per droplet; must be &gt; 0
     * @throws IllegalArgumentException if {@code maxSteps <= 0}
     */
    public HydraulicErosion(int maxSteps) {
        this(maxSteps, 0.2f);
    }

    /**
     * Creates an erosion simulator with configurable step count and gravity.
     *
     * @param maxSteps the maximum number of simulation steps per droplet; must be &gt; 0
     * @param gravity  gravitational acceleration per simulation step
     * @throws IllegalArgumentException if {@code maxSteps <= 0}
     */
    public HydraulicErosion(int maxSteps, float gravity) {
        if (maxSteps <= 0) throw new IllegalArgumentException("maxSteps must be > 0");
        this.maxSteps = maxSteps;
        this.gravity = gravity;
    }

    /**
     * Applies hydraulic erosion to a heightmap. The heightmap is modified in place.
     *
     * @param heightmap  a flat float array of size {@code size * size}
     * @param size       the width/height of the square heightmap
     * @param iterations number of droplets to simulate
     * @param seed       random seed for droplet positions
     */
    public void erode(float[] heightmap, int size, int iterations, long seed) {
        var rng = new SplittableRandom(seed);

        for (int i = 0; i < iterations; i++) {
            float posX = rng.nextFloat() * (size - 1);
            float posZ = rng.nextFloat() * (size - 1);
            float velX = 0.0f;
            float velZ = 0.0f;
            float water = 1.0f;
            float sediment = 0.0f;

            for (int step = 0; step < maxSteps; step++) {
                int xi = clamp((int) posX, 0, size - 1);
                int zi = clamp((int) posZ, 0, size - 1);
                float h = heightmap[zi * size + xi];

                // Calculate gradient
                float gradX = getHeight(heightmap, size, xi + 1, zi)
                        - getHeight(heightmap, size, xi - 1, zi);
                float gradZ = getHeight(heightmap, size, xi, zi + 1)
                        - getHeight(heightmap, size, xi, zi - 1);

                // Update velocity with configurable gravity
                velX = velX * 0.9f - gradX * gravity;
                velZ = velZ * 0.9f - gradZ * gravity;

                posX += velX;
                posZ += velZ;

                float speed = (float) Math.sqrt(velX * velX + velZ * velZ);
                if (speed < MIN_VELOCITY) break;
                if (posX < 0 || posX >= size || posZ < 0 || posZ >= size) break;

                int newXi = clamp((int) posX, 0, size - 1);
                int newZi = clamp((int) posZ, 0, size - 1);
                float newH = heightmap[newZi * size + newXi];
                float dh = newH - h;

                float capacity = Math.max(-dh, 0.0f) * speed * SEDIMENT_CAPACITY * water;
                if (sediment > capacity) {
                    float deposit = (sediment - capacity) * DEPOSITION;
                    int idx = zi * size + xi;
                    heightmap[idx] += deposit;
                    sediment -= deposit;
                } else {
                    float erode = Math.min((capacity - sediment) * EROSION, -dh * 0.5f);
                    if (erode > 0) {
                        int idx = zi * size + xi;
                        heightmap[idx] -= erode;
                        sediment += erode;
                    }
                }

                water *= (1.0f - EVAPORATION);
                if (water < 0.01f) break;
            }
        }
    }

    /**
     * Returns the maximum number of simulation steps per droplet.
     *
     * @return the configured maxSteps value
     */
    public int maxSteps() {
        return maxSteps;
    }

    private static float getHeight(float[] heightmap, int size, int x, int z) {
        if (x < 0 || x >= size || z < 0 || z >= size) {
            return heightmap[clamp(z, 0, size - 1) * size + clamp(x, 0, size - 1)];
        }
        return heightmap[z * size + x];
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
