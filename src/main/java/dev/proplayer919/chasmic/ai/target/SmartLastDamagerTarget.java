package dev.proplayer919.chasmic.ai.target;

import dev.proplayer919.chasmic.ai.AIBehaviorRules;
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
        if (AIBehaviorRules.shouldAvoidCombat(creature, profile)) {
            return null;
        }

        Entity lastAttacker = creature.getLastAttacker();
        if (!isValidTarget(lastAttacker)) {
            return null;
        }

        double distance = creature.getPosition().distance(lastAttacker.getPosition());
        return distance <= profile.getDetectionRange() ? lastAttacker : null;
    }

    @Override
    public boolean isValidTarget(Entity entity) {
        if (!AITargetingRules.isCommonlyValidTarget(creature, entity)) {
            return false;
        }

        double distance = creature.getPosition().distance(entity.getPosition());
        return distance <= profile.getDetectionRange();
    }

    @Override
    public int getPriority() {
        return 1; // Highest priority - self-defense
    }
}
