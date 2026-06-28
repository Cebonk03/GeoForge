package com.geoforge.api.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("FoliaDetector tests")
class FoliaDetectorTest {

    @DisplayName("isFolia returns false without Folia on classpath")
    @Test
    void isFolia_returnsFalse_withoutFoliaOnClasspath() {
        assertThat(FoliaDetector.isFolia()).isFalse();
    }

    @DisplayName("isFolia is deterministic (cached)")
    @Test
    void isFolia_deterministic() {
        boolean first = FoliaDetector.isFolia();
        for (int i = 0; i < 10; i++) {
            assertEquals(first, FoliaDetector.isFolia());
        }
    }
}
