package com.geoforge.engine.feature.tree;

/**
 * Result of a trunk placement operation.
 * Indicates where the trunk tip (canopy start point) ended up
 * and how many blocks were actually placed.
 */
public record TrunkResult(int tipX, int tipY, int tipZ, int placedHeight) {
}
