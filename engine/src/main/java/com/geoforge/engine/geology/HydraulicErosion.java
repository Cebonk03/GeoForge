package com.geoforge.engine.geology;

import java.util.SplittableRandom;

/**
 * Hydraulic erosion simulation using a droplet-based approach.
 *
 * <p>This class contains only stateless methods. The heightmap array is provided by the
 * caller and modified in-place. No state is stored in this class.
 */
public final class HydraulicErosion {

    private static final float MIN_VELOCITY = 0.01f;
    private static final float GRAVITY = 0.2f;
    private static final float EVAPORATION = 0.01f;
    private static final float SEDIMENT_CAPACITY = 8.0f;
    private static final float DEPOSITION = 0.5f;
    private static final float EROSION = 0.5f;

    private HydraulicErosion() {}

    /**
     * Applies hydraulic erosion to a heightmap. The heightmap is modified in place.
     *
     * @param heightmap  a flat float array of size {@code size * size}
     * @param size       the width/height of the square heightmap
     * @param iterations number of droplets to simulate
     * @param seed       random seed for droplet positions
     */
    public static void erode(float[] heightmap, int size, int iterations, long seed) {
        var rng = new SplittableRandom(seed);

        for (int i = 0; i < iterations; i++) {
            // Start droplet at random position
            float posX = rng.nextFloat() * (size - 1);
            float posZ = rng.nextFloat() * (size - 1);
            float velX = 0.0f;
            float velZ = 0.0f;
            float water = 1.0f;
            float sediment = 0.0f;

            for (int step = 0; step < 30; step++) {
                int xi = clamp((int) posX, 0, size - 1);
                int zi = clamp((int) posZ, 0, size - 1);
                float h = heightmap[xi * size + zi];

                // Calculate gradient
                float gradX = getHeight(heightmap, size, xi + 1, zi)
                        - getHeight(heightmap, size, xi - 1, zi);
                float gradZ = getHeight(heightmap, size, xi, zi + 1)
                        - getHeight(heightmap, size, xi, zi - 1);

                // Update velocity
                velX = velX * 0.9f - gradX * GRAVITY;
                velZ = velZ * 0.9f - gradZ * GRAVITY;

                // Move droplet
                posX += velX;
                posZ += velZ;

                // Check stop condition
                float speed = (float) Math.sqrt(velX * velX + velZ * velZ);
                if (speed < MIN_VELOCITY) break;

                // Check bounds
                if (posX < 0 || posX >= size || posZ < 0 || posZ >= size) break;

                int newXi = clamp((int) posX, 0, size - 1);
                int newZi = clamp((int) posZ, 0, size - 1);
                float newH = heightmap[newXi * size + newZi];
                float dh = newH - h;

                float capacity = Math.max(-dh, 0.0f) * speed * SEDIMENT_CAPACITY * water;
                if (sediment > capacity) {
                    float deposit = (sediment - capacity) * DEPOSITION;
                    int idx = xi * size + zi;
                    heightmap[idx] += deposit;
                    sediment -= deposit;
                } else {
                    float erode = Math.min((capacity - sediment) * EROSION, -dh * 0.5f);
                    if (erode > 0) {
                        int idx = xi * size + zi;
                        heightmap[idx] -= erode;
                        sediment += erode;
                    }
                }

                // Evaporation
                water *= (1.0f - EVAPORATION);
                if (water < 0.01f) break;
            }
        }
    }

    private static float getHeight(float[] heightmap, int size, int x, int z) {
        if (x < 0 || x >= size || z < 0 || z >= size) {
            return heightmap[clamp(x, 0, size - 1) * size + clamp(z, 0, size - 1)];
        }
        return heightmap[x * size + z];
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
