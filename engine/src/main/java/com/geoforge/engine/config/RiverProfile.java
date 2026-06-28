package com.geoforge.engine.config;

import java.util.Map;

/**
 * Defines the available river valley carving profiles.
 *
 * <p>Each profile determines the shape and behavior of river carving:
 * <ul>
 *   <li>{@link #VSHAPED} — classic V-shaped valley via simplex noise moisture convergence</li>
 *   <li>{@link #CANYON} — steep-walled canyon carving with configurable depth and width</li>
 *   <li>{@link #FLOODPLAIN} — wide, shallow floodplain carving with flat valley floors</li>
 * </ul>
 */
public enum RiverProfile {
    VSHAPED,
    CANYON,
    FLOODPLAIN;

    private static final Map<String, RiverProfile> NAME_MAP = Map.of(
            "vshaped", VSHAPED,
            "canyon", CANYON,
            "floodplain", FLOODPLAIN
    );

    /**
     * Parses a string into a RiverProfile, case-sensitive lowercase.
     *
     * @param s the profile name (e.g., "vshaped", "canyon", "floodplain")
     * @return the matching RiverProfile
     * @throws IllegalArgumentException if the string does not match a known profile
     */
    public static RiverProfile fromString(String s) {
        if (s == null) {
            throw new IllegalArgumentException("River profile must not be null");
        }
        RiverProfile profile = NAME_MAP.get(s.toLowerCase());
        if (profile == null) {
            throw new IllegalArgumentException("Unknown river profile: '" + s + "'");
        }
        return profile;
    }
}
