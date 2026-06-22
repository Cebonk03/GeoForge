package com.geoforge.api.version;

import java.util.regex.Pattern;

/**
 * A parsed Minecraft version string, split into major, minor, and patch components.
 *
 * <p>Handles the version formats used by both legacy (1.x.y) and modern (26.x.y) Paper
 * releases. Parsing is done via regex to avoid fragile prefix matching.
 *
 * @param major the major version number
 * @param minor the minor version number
 * @param patch the patch version number (0 if absent in the input string)
 */
public record ServerVersion(int major, int minor, int patch) {

    private static final Pattern PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?$");

    /**
     * Parses a raw Minecraft version string into a {@code ServerVersion}.
     *
     * @param raw the version string, e.g. {@code "1.21.4"}, {@code "26.1.2"}
     * @return the parsed version record
     * @throws IllegalArgumentException if the string does not match the expected format
     */
    public static ServerVersion parse(String raw) {
        var matcher = PATTERN.matcher(raw.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Cannot parse Minecraft version: '" + raw + "'");
        }
        return new ServerVersion(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 0);
    }
}
