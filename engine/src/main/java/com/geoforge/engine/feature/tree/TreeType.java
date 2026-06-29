package com.geoforge.engine.feature.tree;

/**
 * Supported vanilla tree types with their associated block material names.
 *
 * <p>Each constant specifies the log and leaves material names used when placing
 * the tree in the world. These names match Minecraft registry keys.
 */
public enum TreeType {

    OAK("oak_log", "oak_leaves"),
    BIRCH("birch_log", "birch_leaves"),
    DARK_OAK("dark_oak_log", "dark_oak_leaves"),
    JUNGLE("jungle_log", "jungle_leaves"),
    SPRUCE("spruce_log", "spruce_leaves"),
    ACACIA("acacia_log", "acacia_leaves"),
    PALE_OAK("pale_oak_log", "pale_oak_leaves"),
    CHERRY("cherry_log", "cherry_leaves"),
    MANGROVE("mangrove_log", "mangrove_leaves");

    private final String logName;
    private final String leavesName;

    TreeType(String logName, String leavesName) {
        this.logName = logName;
        this.leavesName = leavesName;
    }

    /**
     * Returns the Minecraft registry name for the log block of this tree type.
     *
     * @return log material name, e.g. {@code "oak_log"}
     */
    public String logName() {
        return logName;
    }

    /**
     * Returns the Minecraft registry name for the leaves block of this tree type.
     *
     * @return leaves material name, e.g. {@code "oak_leaves"}
     */
    public String leavesName() {
        return leavesName;
    }
}
