package com.geoforge.api.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("ServerVersion parsing tests")
class ServerVersionTest {

    @DisplayName("Parse 1.21.4 correctly splits into major/minor/patch")
    @Test
    void parse_1_21_4() {
        var v = ServerVersion.parse("1.21.4");
        assertThat(v.major()).isEqualTo(1);
        assertThat(v.minor()).isEqualTo(21);
        assertThat(v.patch()).isEqualTo(4);
    }

    @DisplayName("Parse 1.21.11 correctly")
    @Test
    void parse_1_21_11() {
        var v = ServerVersion.parse("1.21.11");
        assertThat(v.major()).isEqualTo(1);
        assertThat(v.minor()).isEqualTo(21);
        assertThat(v.patch()).isEqualTo(11);
    }

    @DisplayName("Parse 26.1 (no patch) defaults patch to 0")
    @Test
    void parse_26_1() {
        var v = ServerVersion.parse("26.1");
        assertThat(v.major()).isEqualTo(26);
        assertThat(v.minor()).isEqualTo(1);
        assertThat(v.patch()).isEqualTo(0);
    }

    @DisplayName("Parse 26.1.2 correctly")
    @Test
    void parse_26_1_2() {
        var v = ServerVersion.parse("26.1.2");
        assertThat(v.major()).isEqualTo(26);
        assertThat(v.minor()).isEqualTo(1);
        assertThat(v.patch()).isEqualTo(2);
    }

    @DisplayName("Parse 26.2 correctly")
    @Test
    void parse_26_2() {
        var v = ServerVersion.parse("26.2");
        assertThat(v.major()).isEqualTo(26);
        assertThat(v.minor()).isEqualTo(2);
        assertThat(v.patch()).isEqualTo(0);
    }

    @DisplayName("Parse 1.21 (no patch) defaults patch to 0")
    @Test
    void parse_1_21() {
        var v = ServerVersion.parse("1.21");
        assertThat(v.major()).isEqualTo(1);
        assertThat(v.minor()).isEqualTo(21);
        assertThat(v.patch()).isEqualTo(0);
    }

    @DisplayName("Parse garbage input throws exception")
    @Test
    void parse_garbage_throws() {
        assertThrows(IllegalArgumentException.class, () -> ServerVersion.parse("garbage"));
    }
}
