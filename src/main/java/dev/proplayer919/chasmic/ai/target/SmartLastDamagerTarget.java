package dev.proplayer919.chasmic.ai.target;

import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;

/**
 * Targets the entity that last damaged the creature.
 * All creatures should use this for self-defense, regardless of aggression level.
 */
public class SmartLastDamagerTarget implements AITarget {
    private final CustomCreature creature;
    private final AIProfile profile;

    public SmartLastDamagerTarget(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
    }

    @Override
    public Entity findTarget() {
        // Check if creature is too shy to fight back
        if (profile.getShyness() > 0.7f) {
            float healthPercent = (float) creature.getCustomHealth() / creature.getCreatureType().maxHealth();
            if (healthPercent < profile.getFleeHealthThreshold()) {
                return null; // Too scared, will flee instead
            }
        }

        // Get the last attacker
        Entity lastAttacker = creature.getLastAttacker();
        if (lastAttacker != null && !lastAttacker.isRemoved()) {
            double distance = creature.getPosition().distance(lastAttacker.getPosition());
            if (distance <= profile.getDetectionRange()) {
                return lastAttacker;
            }
        }

        return null;
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority - self-defense
    }
}


