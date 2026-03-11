package dev.proplayer919.chasmic.entities.ai.target;

import net.minestom.server.entity.Entity;

/**
 * Custom AI target interface for finding and selecting targets.
 * Targets are evaluated to determine what entity a creature should focus on.
 */
public interface AITarget {
    /**
     * Find a suitable target based on the target's criteria.
     * @return the entity to target, or null if no suitable target found
     */
    Entity findTarget();

    /**
     * Returns whether an existing target is still valid for this selector.
     */
    default boolean isValidTarget(Entity entity) {
        return entity != null && !entity.isRemoved();
    }

    /**
     * Get the priority of this target selector. Higher priority targets are evaluated first.
     * Priority levels:
     * - 1: Highest (last attacker, self-defense)
     * - 2: High (defending allies)
     * - 3: Medium (aggressive hunting)
     * - 4: Low (opportunistic targeting)
     *
     * @return the priority level
     */
    int getPriority();
}
