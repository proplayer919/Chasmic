package dev.proplayer919.chasmic.ai.goal;

import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.Navigator;

/**
 * Makes the creature flee from its target when it's too scared or injured.
 * Triggered by high shyness values or low health.
 */
public class FleeGoal implements AIGoal {
    private final CustomCreature creature;
    private final AIProfile profile;
    private final Navigator navigator;
    private static final double FLEE_DISTANCE = 16.0;
    private boolean active = false;

    public FleeGoal(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
        this.navigator = creature.getNavigator();
    }

    @Override
    public boolean canStart() {
        // Check if creature is shy enough to flee
        if (profile.getShyness() < 0.3f) {
            return false; // Not shy enough to flee
        }

        Entity target = creature.getTarget();
        if (target == null) {
            return false;
        }

        // Check health threshold
        float healthPercent = (float) creature.getCustomHealth() / creature.getCreatureType().maxHealth();
        if (healthPercent > profile.getFleeHealthThreshold()) {
            return false; // Health is still okay
        }

        // Check distance - only flee if enemy is close
        double distance = creature.getPosition().distance(target.getPosition());
        return distance < FLEE_DISTANCE;
    }

    @Override
    public void start() {
        active = true;
    }

    @Override
    public void tick() {
        if (!active) return;

        Entity target = creature.getTarget();
        if (target == null) {
            return;
        }

        // Calculate flee direction (opposite of target)
        var creaturePos = creature.getPosition();
        var targetPos = target.getPosition();

        double dx = creaturePos.x() - targetPos.x();
        double dz = creaturePos.z() - targetPos.z();

        // Normalize and scale by flee distance
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length > 0) {
            dx = (dx / length) * FLEE_DISTANCE;
            dz = (dz / length) * FLEE_DISTANCE;
        }

        // Move away from target
        var fleePos = creaturePos.add(dx, 0, dz);
        navigator.setPathTo(fleePos);
    }

    @Override
    public boolean shouldEnd() {
        Entity target = creature.getTarget();
        if (target == null || target.isRemoved()) {
            return true;
        }

        // Stop fleeing if far enough away
        double distance = creature.getPosition().distance(target.getPosition());
        if (distance > FLEE_DISTANCE * 1.5) {
            return true;
        }

        // Stop fleeing if health is restored above threshold
        float healthPercent = (float) creature.getCustomHealth() / creature.getCreatureType().maxHealth();
        return healthPercent > (profile.getFleeHealthThreshold() + 0.2f);
    }

    @Override
    public void end() {
        active = false;
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority - survival
    }

    @Override
    public boolean isActive() {
        return active;
    }
}



