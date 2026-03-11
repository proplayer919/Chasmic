package dev.proplayer919.chasmic.player.permission;

import org.jspecify.annotations.NonNull;

/**
 * Represents a permission node in a hierarchical permission system
 * Supports wildcard permissions (e.g., "rank.*", "*")
 *
 * @param value true = allow, false = deny
 */
public record Permission(String node, boolean value) {
    public Permission(String node, boolean value) {
        this.node = node.toLowerCase();
        this.value = value;
    }

    public Permission(String node) {
        this(node, true);
    }

    /**
     * Checks if this permission matches the given permission node
     * Supports wildcard matching (e.g., "rank.*" matches "rank.give")
     */
    public boolean matches(String checkNode) {
        checkNode = checkNode.toLowerCase();

        // Exact match
        if (node.equals(checkNode)) {
            return true;
        }

        // Full wildcard
        if (node.equals("*")) {
            return true;
        }

        // Wildcard at end (e.g., "rank.*")
        if (node.endsWith(".*")) {
            String prefix = node.substring(0, node.length() - 2);
            return checkNode.startsWith(prefix + ".");
        }

        // Wildcard in middle (e.g., "rank.*.give")
        if (node.contains("*")) {
            String regex = node.replace(".", "\\.").replace("*", ".*");
            return checkNode.matches(regex);
        }

        return false;
    }

    @Override
    public @NonNull String toString() {
        return (value ? "" : "-") + node;
    }
}

