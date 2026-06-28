package com.geoforge.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter;
import com.geoforge.adapters.v26_x.Paper26xAdapter;
import com.geoforge.api.adapter.VanillaFallbackAdapter;
import com.geoforge.api.version.ServerVersion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("Adapter factory tests")
class AdapterFactoryTest {

    @DisplayName("Major version 1 (1.21.x) returns Paper1_21_x adapter")
    @Test
    void selectClass_1_21_4_returnsPaper1_21_x() {
        var v = ServerVersion.parse("1.21.4");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(Paper1_21_xAdapter.class);
    }

    @DisplayName("Version 1.21.11 maps to Paper1_21_x adapter")
    @Test
    void selectClass_1_21_11_returnsPaper1_21_x() {
        var v = ServerVersion.parse("1.21.11");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(Paper1_21_xAdapter.class);
    }

    @DisplayName("Version 26.x maps to Paper26x adapter")
    @Test
    void selectClass_26_1_2_returnsPaper26x() {
        var v = ServerVersion.parse("26.1.2");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(Paper26xAdapter.class);
    }

    @DisplayName("Version 3.x returns VanillaFallback adapter")
    @Test
    void selectClass_3_5_returnsVanillaFallback() {
        var v = ServerVersion.parse("3.5");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(VanillaFallbackAdapter.class);
    }

    @DisplayName("Unknown version 1.22 returns VanillaFallback adapter")
    @Test
    void selectClass_1_22_returnsVanillaFallback() {
        var v = ServerVersion.parse("1.22.0");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(VanillaFallbackAdapter.class);
    }

    @DisplayName("Version 27.x maps to Paper26x adapter")
    @Test
    void selectClass_27_0_returnsPaper26x() {
        var v = ServerVersion.parse("27.0.0");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(Paper26xAdapter.class);
    }

    @DisplayName("Version 2.x returns VanillaFallback adapter")
    @Test
    void selectClass_2_0_returnsVanillaFallback() {
        var v = ServerVersion.parse("2.0.0");
        assertThat(AdapterFactory.selectClass(v)).isEqualTo(VanillaFallbackAdapter.class);
    }
}
