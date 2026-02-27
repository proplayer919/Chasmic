package dev.proplayer919.chasmic.permission;

import java.util.*;

/**
 * Represents a permission node in a hierarchical permission system
 * Supports wildcard permissions (e.g., "rank.*", "*")
 */
public class Permission {
    private final String node;
    private final boolean value; // true = allow, false = deny

    public Permission(String node, boolean value) {
        this.node = node.toLowerCase();
        this.value = value;
    }

    public Permission(String node) {
        this(node, true);
    }

    public String getNode() {
        return node;
    }

    public boolean getValue() {
        return value;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return value == that.value && Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, value);
    }

    @Override
    public String toString() {
        return (value ? "" : "-") + node;
    }
}

