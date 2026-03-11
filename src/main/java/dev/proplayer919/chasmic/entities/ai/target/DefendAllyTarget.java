package dev.proplayer919.chasmic.entities.ai.target;

import dev.proplayer919.chasmic.entities.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;

import java.util.Comparator;

/**
 * Targets enemies that are attacking allies.
 * Only loyal creatures will help their allies in combat.
 */
public class DefendAllyTarget implements AITarget {
    private final CustomCreature creature;
    private final AIProfile profile;

    public DefendAllyTarget(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
    }

    @Override
    public Entity findTarget() {
        if (creature.getInstance() == null) {
            return null;
        }
        if (!AIBehaviorRules.isTraitActive(profile.getLoyalty())) {
            return null;
        }
        if (AIBehaviorRules.shouldAvoidCombat(creature, profile)) {
            return null;
        }

        return creature.getInstance()
                .getNearbyEntities(creature.getPosition(), profile.getDetectionRange())
                .stream()
                .filter(e -> e instanceof CustomCreature)
                .map(CustomCreature.class::cast)
                .filter(ally -> ally != creature)
                .filter(this::isSameCreatureType)
                .map(CustomCreature::getLastAttacker)
                .filter(this::isValidTarget)
                .min(Comparator
                        .comparingDouble(this::distanceToCreature)
                        .thenComparingInt(Entity::getEntityId))
                .orElse(null);
    }

    private boolean isSameCreatureType(CustomCreature other) {
        return other.getCreatureType().id().equals(creature.getCreatureType().id());
    }

    private double distanceToCreature(Entity entity) {
        return creature.getPosition().distance(entity.getPosition());
    }

    @Override
    public boolean isValidTarget(Entity entity) {
        if (!AITargetingRules.isCommonlyValidTarget(creature, entity)) {
            return false;
        }

        return distanceToCreature(entity) <= profile.getDetectionRange();
    }

    @Override
    public int getPriority() {
        return 2; // High priority - defending allies
    }
}
