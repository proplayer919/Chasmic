package dev.proplayer919.chasmic.ai.target;

import dev.proplayer919.chasmic.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;

import java.util.Comparator;

/**
 * Intelligently targets players based on the creature's aggression level.
 * More aggressive creatures will actively seek out players from farther away.
 */
public class AggressivePlayerTarget implements AITarget {
    private final CustomCreature creature;
    private final AIProfile profile;

    public AggressivePlayerTarget(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
    }

    @Override
    public Entity findTarget() {
        if (creature.getInstance() == null) {
            return null;
        }
        if (!AIBehaviorRules.isTraitActive(profile.getAggressiveness())) {
            return null;
        }
        if (AIBehaviorRules.shouldAvoidCombat(creature, profile)) {
            return null;
        }

        double searchRange = profile.getDetectionRange() * profile.getAggressiveness();
        Entity currentTarget = creature.getTarget();

        // Stick to the current target while still valid and reasonably close.
        if (isValidTarget(currentTarget) && currentTarget != null) {
            double stickyRange = searchRange * AIProfile.TARGET_STICKINESS_RANGE_MULTIPLIER;
            double distance = creature.getPosition().distance(currentTarget.getPosition());
            if (distance <= stickyRange) {
                return currentTarget;
            }
        }

        return creature.getInstance()
                .getNearbyEntities(creature.getPosition(), searchRange)
                .stream()
                .filter(this::isValidTarget)
                .min(Comparator
                        .comparingDouble(this::distanceToCreature)
                        .thenComparingInt(Entity::getEntityId))
                .orElse(null);
    }

    private double distanceToCreature(Entity entity) {
        return creature.getPosition().distance(entity.getPosition());
    }

    @Override
    public boolean isValidTarget(Entity entity) {
        if (!AITargetingRules.isValidPlayerTarget(creature, profile, entity)) {
            return false;
        }

        double searchRange = profile.getDetectionRange() * profile.getAggressiveness();
        return distanceToCreature(entity) <= searchRange * AIProfile.TARGET_STICKINESS_RANGE_MULTIPLIER;
    }

    @Override
    public int getPriority() {
        return 3; // Medium priority - aggressive hunting
    }
}
