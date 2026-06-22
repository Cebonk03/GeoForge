package com.geoforge.engine.plateau;

/**
 * Utility for flattening terrain into plateaus, typically for structure placement.
 *
 * <p>All methods are stateless and operate on caller-owned heightmap arrays.
 */
public final class StructurePlateauModifier {

    private static final int FEATHER_WIDTH = 3;
    private static final float FEATHER_STEP = 1.0f / (FEATHER_WIDTH + 1);

    private StructurePlateauModifier() {}

    /**
     * Flattens a rectangular region of the heightmap to the given target height, with a
     * feathered border.
     *
     * @param heightmap    the heightmap array (size {@code size × size})
     * @param size         the width/height of the square heightmap
     * @param x0           minimum x of the plateau region (inclusive)
     * @param z0           minimum z of the plateau region (inclusive)
     * @param x1           maximum x of the plateau region (inclusive)
     * @param z1           maximum z of the plateau region (inclusive)
     * @param targetHeight the height to flatten to
     */
    public static void applyPlateau(
            float[] heightmap,
            int size,
            int x0,
            int z0,
            int x1,
            int z1,
            float targetHeight) {

        int cx0 = clamp(x0, 0, size - 1);
        int cz0 = clamp(z0, 0, size - 1);
        int cx1 = clamp(x1, 0, size - 1);
        int cz1 = clamp(z1, 0, size - 1);

        // First pass — set interior to target height
        for (int z = cz0; z <= cz1; z++) {
            for (int x = cx0; x <= cx1; x++) {
                heightmap[z * size + x] = targetHeight;
            }
        }

        // Second pass — feather the border
        for (int z = cz0; z <= cz1; z++) {
            for (int x = cx0; x <= cx1; x++) {
                float feather = computeFeather(x, z, cx0, cz0, cx1, cz1);
                if (feather < 1.0f) {
                    float neighbor = getNeighborAvg(heightmap, size, x, z, cx0, cz0, cx1, cz1);
                    float blended = neighbor * (1.0f - feather) + targetHeight * feather;
                    heightmap[z * size + x] = blended;
                }
            }
        }
    }

    private static float computeFeather(
            int x, int z, int cx0, int cz0, int cx1, int cz1) {
        int dx = Math.min(Math.abs(x - cx0), Math.abs(x - cx1));
        int dz = Math.min(Math.abs(z - cz0), Math.abs(z - cz1));
        int dist = Math.min(dx, dz);
        if (dist >= FEATHER_WIDTH) return 1.0f;
        return dist * FEATHER_STEP;
    }

    private static float getNeighborAvg(
            float[] heightmap,
            int size,
            int x,
            int z,
            int cx0,
            int cz0,
            int cx1,
            int cz1) {
        float sum = 0.0f;
        int count = 0;

        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dz == 0) continue;
                int nx = x + dx;
                int nz = z + dz;
                if (nx < cx0 || nx > cx1 || nz < cz0 || nz > cz1) {
                    // Outside plateau — use original height
                    if (nx >= 0 && nx < size && nz >= 0 && nz < size) {
                        sum += heightmap[nz * size + nx];
                        count++;
                    }
                }
            }
        }
        return count > 0 ? sum / count : heightmap[z * size + x];
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
