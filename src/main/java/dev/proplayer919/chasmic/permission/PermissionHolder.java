package dev.proplayer919.chasmic.permission;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages permissions for players
 * Supports hierarchical permission checking with wildcards
 */
public class PermissionHolder {
    private final Set<Permission> permissions = ConcurrentHashMap.newKeySet();

    /**
     * Adds a permission to this holder
     */
    public void addPermission(String node) {
        permissions.add(new Permission(node, true));
    }

    /**
     * Adds a permission with a specific value
     */
    public void addPermission(String node, boolean value) {
        permissions.add(new Permission(node, value));
    }

    /**
     * Adds a permission object
     */
    public void addPermission(Permission permission) {
        permissions.add(permission);
    }

    /**
     * Removes a permission from this holder
     */
    public void removePermission(String node) {
        permissions.removeIf(p -> p.getNode().equalsIgnoreCase(node));
    }

    /**
     * Checks if this holder has a specific permission
     * @param node The permission node to check (e.g., "rank.give.pro")
     * @return true if the player has the permission, false otherwise
     */
    public boolean hasPermission(String node) {
        node = node.toLowerCase();

        // Check for explicit deny first (takes priority)
        for (Permission permission : permissions) {
            if (permission.matches(node) && !permission.getValue()) {
                return false;
            }
        }

        // Check for explicit allow
        for (Permission permission : permissions) {
            if (permission.matches(node) && permission.getValue()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets all permissions
     */
    public Set<Permission> getPermissions() {
        return new HashSet<>(permissions);
    }

    /**
     * Gets all permission nodes as strings
     */
    public List<String> getPermissionNodes() {
        List<String> nodes = new ArrayList<>();
        for (Permission permission : permissions) {
            nodes.add(permission.toString());
        }
        return nodes;
    }

    /**
     * Clears all permissions
     */
    public void clearPermissions() {
        permissions.clear();
    }

    /**
     * Loads permissions from a list of permission strings
     */
    public void loadPermissions(List<String> permissionStrings) {
        clearPermissions();
        if (permissionStrings != null) {
            for (String permString : permissionStrings) {
                if (permString.startsWith("-")) {
                    addPermission(permString.substring(1), false);
                } else {
                    addPermission(permString, true);
                }
            }
        }
    }
}

