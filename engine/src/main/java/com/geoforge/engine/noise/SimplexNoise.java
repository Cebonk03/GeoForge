package com.geoforge.engine.noise;

import java.util.SplittableRandom;

/**
 * Multi-octave gradient noise generator with deterministic sampling.
 *
 * <p>Uses a shuffled permutation table with gradient vectors at integer lattice points,
 * smoothly interpolated with a fade curve. This implementation produces values in [-1, 1]
 * and is continuous (C1 across lattice boundaries).
 */
public final class SimplexNoise implements NoiseSource {

    private static final int PERM_MASK = 255;
    private static final int PERM_SIZE = 256;

    private final int[] perm;

    /** Creates a noise instance with the given seed. */
    public SimplexNoise(long seed) {
        this.perm = new int[PERM_SIZE * 2];
        var rng = new SplittableRandom(seed);
        var table = new int[PERM_SIZE];
        for (int i = 0; i < PERM_SIZE; i++) {
            table[i] = i;
        }
        for (int i = PERM_SIZE - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = table[i];
            table[i] = table[j];
            table[j] = tmp;
        }
        for (int i = 0; i < PERM_SIZE * 2; i++) {
            perm[i] = table[i & PERM_MASK];
        }
    }

    @Override
    public double sample2D(double x, double z) {
        return sample(x, z);
    }

    @Override
    public double sample3D(double x, double y, double z) {
        return sample(x, y, z);
    }

    /** 2D noise sample at (x, z). */
    public double sample(double x, double z) {
        int xi = fastFloor(x);
        int zi = fastFloor(z);
        double xf = x - xi;
        double zf = z - zi;

        double u = fade(xf);
        double v = fade(zf);

        int aa = perm[perm[xi & PERM_MASK] + (zi & PERM_MASK)];
        int ab = perm[perm[xi & PERM_MASK] + ((zi + 1) & PERM_MASK)];
        int ba = perm[perm[(xi + 1) & PERM_MASK] + (zi & PERM_MASK)];
        int bb = perm[perm[(xi + 1) & PERM_MASK] + ((zi + 1) & PERM_MASK)];

        double x1 = lerp(grad2D(aa, xf, zf), grad2D(ba, xf - 1, zf), u);
        double x2 = lerp(grad2D(ab, xf, zf - 1), grad2D(bb, xf - 1, zf - 1), u);
        return lerp(x1, x2, v);
    }

    /** 3D noise sample at (x, y, z). */
    public double sample(double x, double y, double z) {
        int xi = fastFloor(x);
        int yi = fastFloor(y);
        int zi = fastFloor(z);
        double xf = x - xi;
        double yf = y - yi;
        double zf = z - zi;

        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        int xm = xi & PERM_MASK;
        int ym = yi & PERM_MASK;
        int zm = zi & PERM_MASK;

        int a0 = perm[xm] + ym;
        int b0 = perm[(xm + 1) & PERM_MASK] + ym;

        int aa = perm[a0] + zm;
        int ab = perm[a0 + 1] + zm;
        int ba = perm[b0] + zm;
        int bb = perm[b0 + 1] + zm;

        double x1 = lerp(grad3D(perm[aa], xf, yf, zf),
                grad3D(perm[ba], xf - 1, yf, zf), u);
        double x2 = lerp(grad3D(perm[ab], xf, yf - 1, zf),
                grad3D(perm[bb], xf - 1, yf - 1, zf), u);
        double y1 = lerp(x1, x2, v);

        double x3 = lerp(grad3D(perm[aa + 1], xf, yf, zf - 1),
                grad3D(perm[ba + 1], xf - 1, yf, zf - 1), u);
        double x4 = lerp(grad3D(perm[ab + 1], xf, yf - 1, zf - 1),
                grad3D(perm[bb + 1], xf - 1, yf - 1, zf - 1), u);
        double y2 = lerp(x3, x4, v);

        return lerp(y1, y2, w);
    }

    private static int fastFloor(double x) {
        int xi = (int) x;
        return x < xi ? xi - 1 : xi;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static final double[][] GRAD_2D = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
    };

    private static final double[][] GRAD_3D = {
        {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0},
        {0, 0, 1}, {0, 0, -1},
        {1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0},
        {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1},
        {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1},
    };

    private static double grad2D(int hash, double x, double z) {
        double[] g = GRAD_2D[hash & 7];
        return g[0] * x + g[1] * z;
    }

    private static double grad3D(int hash, double x, double y, double z) {
        double[] g = GRAD_3D[hash % GRAD_3D.length];
        return g[0] * x + g[1] * y + g[2] * z;
    }
}
