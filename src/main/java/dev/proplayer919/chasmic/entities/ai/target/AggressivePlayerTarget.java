package dev.proplayer919.chasmic.entities.ai.target;

import dev.proplayer919.chasmic.entities.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.coordinate.Pos;
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
        Pos creaturePos = creature.getPosition();

        // Stick to the current target while still valid and reasonably close.
        if (isValidTarget(currentTarget) && currentTarget != null) {
            double stickyRange = searchRange * AIProfile.TARGET_STICKINESS_RANGE_MULTIPLIER;
            double distance = creaturePos.distance(currentTarget.getPosition());
            if (distance <= stickyRange) {
                return currentTarget;
            }
        }

        // Cache creature position to avoid multiple calls
        return creature.getInstance()
                .getNearbyEntities(creaturePos, searchRange)
                .stream()
                .filter(this::isValidTarget)
                .min(Comparator
                        .<Entity>comparingDouble(entity -> creaturePos.distance(entity.getPosition()))
                        .thenComparingInt(Entity::getEntityId))
                .orElse(null);
    }

    @Override
    public boolean isValidTarget(Entity entity) {
        if (!AITargetingRules.isValidPlayerTarget(creature, profile, entity)) {
            return false;
        }

        double searchRange = profile.getDetectionRange() * profile.getAggressiveness();
        double distance = creature.getPosition().distance(entity.getPosition());
        return distance <= searchRange * AIProfile.TARGET_STICKINESS_RANGE_MULTIPLIER;
    }

    @Override
    public int getPriority() {
        return 3; // Medium priority - aggressive hunting
    }
}
