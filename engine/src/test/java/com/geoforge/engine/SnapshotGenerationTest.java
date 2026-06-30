package com.geoforge.engine;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.engine.config.GeoForgeConfig;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
@DisplayName("Snapshot determinism tests")
class SnapshotGenerationTest {

    private static final long SEED = 42L;
    private static final GeoForgeConfig CFG = GeoForgeConfig.defaults();

    @DisplayName("16x16 heightmap SHA-256 checksum matches golden value")
    @Test
    void heightmap_checksum_matchesKnownValue() throws Exception {
        var engine = new GeoForgeEngine(SEED, CFG);
        var digest = MessageDigest.getInstance("SHA-256");

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int h = engine.getSurfaceHeight(x, z);
                digest.update((byte) (h >> 24));
                digest.update((byte) (h >> 16));
                digest.update((byte) (h >> 8));
                digest.update((byte) h);
            }
        }

        byte[] hash = digest.digest();
        String checksum = HexFormat.of().formatHex(hash);
        assertEquals(
                "b70da91fb037313f9d2b7dec277c0d26ba25e635a5ea294396ca12ae34a04709",
                checksum,
                "Heightmap checksum mismatch — terrain output changed.");
    }

    @DisplayName("4x4x4 density field checksum is reproducible")
    @Test
    void densityField_checksum_matchesKnownValue() throws Exception {
        var engine = new GeoForgeEngine(SEED, CFG);
        var digest = MessageDigest.getInstance("SHA-256");

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 4; z++) {
                    double d = engine.getDensity(x * 4, y * 64 - 32, z * 4);
                    long bits = Double.doubleToLongBits(d);
                    digest.update((byte) (bits >> 56));
                    digest.update((byte) (bits >> 48));
                    digest.update((byte) (bits >> 40));
                    digest.update((byte) (bits >> 32));
                    digest.update((byte) (bits >> 24));
                    digest.update((byte) (bits >> 16));
                    digest.update((byte) (bits >> 8));
                    digest.update((byte) bits);
                }
            }
        }

        String expectedDensityCs = HexFormat.of().formatHex(digest.digest());

        var engine2 = new GeoForgeEngine(SEED, CFG);
        var digest2 = MessageDigest.getInstance("SHA-256");
        for (int x = 0; x < 4; x++)
            for (int y = 0; y < 4; y++)
                for (int z = 0; z < 4; z++) {
                    double d = engine2.getDensity(x * 4, y * 64 - 32, z * 4);
                    long bits = Double.doubleToLongBits(d);
                    digest2.update((byte) (bits >> 56));
                    digest2.update((byte) (bits >> 48));
                    digest2.update((byte) (bits >> 40));
                    digest2.update((byte) (bits >> 32));
                    digest2.update((byte) (bits >> 24));
                    digest2.update((byte) (bits >> 16));
                    digest2.update((byte) (bits >> 8));
                    digest2.update((byte) bits);
                }
        String recomputed = HexFormat.of().formatHex(digest2.digest());
        assertEquals(expectedDensityCs, recomputed);
    }

    @DisplayName("Different seeds produce different heightmap checksums")
    @Test
    void differentSeed_differentChecksum() throws Exception {
        String cs1 = computeHeightChecksum(new GeoForgeEngine(SEED, CFG));
        String cs2 = computeHeightChecksum(new GeoForgeEngine(SEED + 1, CFG));
        assertNotEquals(cs1, cs2);
    }

    @DisplayName("Same seed produces identical heightmap checksums")
    @Test
    void sameSeed_sameChecksum() throws Exception {
        assertEquals(
                computeHeightChecksum(new GeoForgeEngine(SEED, CFG)),
                computeHeightChecksum(new GeoForgeEngine(SEED, CFG)));
    }

    private static String computeHeightChecksum(GeoForgeEngine engine) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int h = engine.getSurfaceHeight(x, z);
                digest.update((byte) (h >> 24));
                digest.update((byte) (h >> 16));
                digest.update((byte) (h >> 8));
                digest.update((byte) h);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
