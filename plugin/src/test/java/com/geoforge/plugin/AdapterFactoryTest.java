package com.geoforge.plugin;

import static org.junit.jupiter.api.Assertions.*;

import com.geoforge.adapters.v1_21_x.Paper1_21_xAdapter;
import com.geoforge.adapters.v26_x.Paper26xAdapter;
import com.geoforge.api.adapter.VanillaFallbackAdapter;
import com.geoforge.api.version.ServerVersion;
import org.junit.jupiter.api.Test;

class AdapterFactoryTest {

    @Test
    void selectClass_1_21_4_returnsPaper1_21_x() {
        var v = ServerVersion.parse("1.21.4");
        assertEquals(Paper1_21_xAdapter.class, AdapterFactory.selectClass(v));
    }

    @Test
    void selectClass_1_21_11_returnsPaper1_21_x() {
        var v = ServerVersion.parse("1.21.11");
        assertEquals(Paper1_21_xAdapter.class, AdapterFactory.selectClass(v));
    }

    @Test
    void selectClass_26_1_2_returnsPaper26x() {
        var v = ServerVersion.parse("26.1.2");
        assertEquals(Paper26xAdapter.class, AdapterFactory.selectClass(v));
    }

    @Test
    void selectClass_3_5_returnsVanillaFallback() {
        var v = ServerVersion.parse("3.5");
        assertEquals(VanillaFallbackAdapter.class, AdapterFactory.selectClass(v));
    }
}
