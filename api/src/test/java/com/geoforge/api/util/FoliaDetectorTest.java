package com.geoforge.api.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FoliaDetectorTest {

    @Test
    void isFolia_returnsFalse_withoutFoliaOnClasspath() {
        assertFalse(FoliaDetector.isFolia(), "FoliaDetector should return false in test environment");
    }

    @Test
    void isFolia_deterministic() {
        boolean first = FoliaDetector.isFolia();
        for (int i = 0; i < 10; i++) {
            assertEquals(first, FoliaDetector.isFolia(),
                    "isFolia() should be deterministic (cached)");
        }
    }
}
