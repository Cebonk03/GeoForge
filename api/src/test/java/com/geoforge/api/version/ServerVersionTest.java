package com.geoforge.api.version;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ServerVersionTest {

    @Test
    void parse_1_21_4() {
        var v = ServerVersion.parse("1.21.4");
        assertEquals(1, v.major());
        assertEquals(21, v.minor());
        assertEquals(4, v.patch());
    }

    @Test
    void parse_1_21_11() {
        var v = ServerVersion.parse("1.21.11");
        assertEquals(1, v.major());
        assertEquals(21, v.minor());
        assertEquals(11, v.patch());
    }

    @Test
    void parse_26_1() {
        var v = ServerVersion.parse("26.1");
        assertEquals(26, v.major());
        assertEquals(1, v.minor());
        assertEquals(0, v.patch());
    }

    @Test
    void parse_26_1_2() {
        var v = ServerVersion.parse("26.1.2");
        assertEquals(26, v.major());
        assertEquals(1, v.minor());
        assertEquals(2, v.patch());
    }

    @Test
    void parse_26_2() {
        var v = ServerVersion.parse("26.2");
        assertEquals(26, v.major());
        assertEquals(2, v.minor());
        assertEquals(0, v.patch());
    }

    @Test
    void parse_1_21() {
        var v = ServerVersion.parse("1.21");
        assertEquals(1, v.major());
        assertEquals(21, v.minor());
        assertEquals(0, v.patch());
    }

    @Test
    void parse_garbage_throws() {
        assertThrows(IllegalArgumentException.class, () -> ServerVersion.parse("garbage"));
    }
}
