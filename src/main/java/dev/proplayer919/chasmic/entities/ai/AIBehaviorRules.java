package dev.proplayer919.chasmic.entities.ai;

import dev.proplayer919.chasmic.entities.CustomCreature;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Shared helper rules so goals and targets interpret profile traits consistently.
 */
public final class AIBehaviorRules {
    private AIBehaviorRules() {
    }

    public static boolean isTraitActive(float value) {
        return value >= AIProfile.MIN_ACTIVE_TRAIT;
    }

    public static float healthPercent(CustomCreature creature) {
        return (float) creature.getCustomHealth() / creature.getCreatureType().maxHealth();
    }

    public static boolean shouldAvoidCombat(CustomCreature creature, AIProfile profile) {
        if (!isTraitActive(profile.getShyness())) {
            return false;
        }

        // More shy creatures disengage earlier by widening the flee threshold.
        float adjustedFleeThreshold = Math.min(
                AIProfile.MAX_TRAIT_VALUE,
                profile.getFleeHealthThreshold() + (profile.getShyness() * 0.25f)
        );
        return healthPercent(creature) <= adjustedFleeThreshold;
    }

    public static boolean rollTraitChance(float traitValue, double baseChancePerTick) {
        if (!isTraitActive(traitValue)) {
            return false;
        }
        double chance = Math.max(0.0d, Math.min(1.0d, traitValue * baseChancePerTick));
        return ThreadLocalRandom.current().nextDouble() < chance;
    }
}

