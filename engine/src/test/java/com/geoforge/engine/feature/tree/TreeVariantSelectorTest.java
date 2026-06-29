package com.geoforge.engine.feature.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.geoforge.engine.feature.tree.trunk.StraightTrunk;
import com.geoforge.engine.feature.tree.trunk.BentTrunk;
import com.geoforge.engine.feature.tree.canopy.RoundCanopy;
import com.geoforge.engine.feature.tree.canopy.SpreadingCanopy;
import com.geoforge.engine.feature.tree.canopy.SparseCanopy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
@DisplayName("TreeVariantSelector tests")
class TreeVariantSelectorTest {

    private static final TreeVariant OAK_ROUND = new TreeVariant(
            "oak_round", new StraightTrunk(), new RoundCanopy(), 4, 7, 1.0, 0.5);
    private static final TreeVariant OAK_SPREADING = new TreeVariant(
            "oak_spreading", new StraightTrunk(), new SpreadingCanopy(), 4, 6, 0.9, 0.3);
    private static final TreeVariant OAK_BENT = new TreeVariant(
            "oak_bent", new BentTrunk(), new SparseCanopy(), 3, 5, 0.5, 0.2);

    @Test @DisplayName("Same seed + position selects same variant")
    void deterministicByPosition() {
        var sel1 = new TreeVariantSelector(42L);
        var sel2 = new TreeVariantSelector(42L);
        var v = List.of(OAK_ROUND, OAK_SPREADING, OAK_BENT);
        assertThat(sel1.select(v, 100, 200).name())
                .isEqualTo(sel2.select(v, 100, 200).name());
    }

    @Test @DisplayName("Null biomeId uses unmodified weights")
    void nullBiomeUsesDefaultWeights() {
        var v = List.of(OAK_ROUND, OAK_SPREADING, OAK_BENT);
        // At seed 42, position (30,0) selects oak_bent
        var sel = new TreeVariantSelector(42L);
        // Try positions in a 100x100 grid to find at least one bent
        boolean sawBent = false;
        outer:
        for (int x = 0; x < 500; x += 7) {
            for (int z = 0; z < 500; z += 7) {
                if ("oak_bent".equals(sel.select(v, x, z, null).name())) {
                    sawBent = true; break outer;
                }
            }
        }
        assertThat(sawBent).isTrue();
    }

    @Test @DisplayName("Biome modifiers suppress variant")
    void biomeModifiersWork() {
        var m = Map.of("forest", Map.of("oak_bent", 0.0));
        var s = new TreeVariantSelector(42L, 0.015, m);
        var v = List.of(OAK_ROUND, OAK_SPREADING, OAK_BENT);
        for (int i = 0; i < 100; i++)
            assertThat(s.select(v, i*10, 0, "forest").name()).isNotEqualTo("oak_bent");
    }

    @Test @DisplayName("Single variant returns that variant")
    void singleVariant() {
        assertThat(new TreeVariantSelector(42L).select(List.of(OAK_ROUND), 100, 200).name())
                .isEqualTo("oak_round");
    }

    @Test @DisplayName("Empty variant list throws")
    void emptyVariantsThrows() {
        assertThatThrownBy(() -> new TreeVariantSelector(42L).select(List.of(), 100, 200))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
