package dev.proplayer919.chasmic.entities.ai;

import dev.proplayer919.chasmic.entities.ai.goal.*;
import dev.proplayer919.chasmic.entities.ai.target.AggressivePlayerTarget;
import dev.proplayer919.chasmic.entities.ai.target.DefendAllyTarget;
import dev.proplayer919.chasmic.entities.ai.target.AITarget;
import dev.proplayer919.chasmic.entities.ai.target.SmartLastDamagerTarget;
import dev.proplayer919.chasmic.entities.CustomCreature;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to build and configure AI for creatures based on their AI profile.
 * Automatically sets up appropriate goals and targets based on personality traits.
 */
public class AIBuilder {
    private final CustomCreature creature;
    private final AIProfile profile;
    private final List<AIGoal> goals = new ArrayList<>();
    private final List<AITarget> targets = new ArrayList<>();

    public AIBuilder(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
    }

    /**
     * Adds all standard goals based on the AI profile.
     * This includes combat, movement, fleeing, and social behaviors.
     */
    public AIBuilder withStandardGoals() {
        if (profile.getShyness() >= AIProfile.MIN_ACTIVE_TRAIT) {
            goals.add(new FleeGoal(creature, profile));
        }

        if (profile.getAggressiveness() >= AIProfile.MIN_ACTIVE_TRAIT) {
            goals.add(new SmartMeleeAttackGoal(creature, profile));
        }

        if (profile.getSociability() >= AIProfile.MIN_ACTIVE_TRAIT) {
            goals.add(new StayNearAlliesGoal(creature, profile));
        }

        if (profile.getCuriosity() >= AIProfile.MIN_ACTIVE_TRAIT) {
            goals.add(new InvestigateGoal(creature, profile));
        }

        if (profile.getWanderlust() >= AIProfile.MIN_ACTIVE_TRAIT) {
            goals.add(new SmartWanderGoal(creature, profile));
        }

        return this;
    }

    /**
     * Adds all standard targeting behaviors based on the AI profile.
     */
    public AIBuilder withStandardTargets() {
        // Always prioritize last damager (self-defense)
        targets.add(new SmartLastDamagerTarget(creature, profile));

        if (profile.getLoyalty() >= AIProfile.MIN_ACTIVE_TRAIT) {
            targets.add(new DefendAllyTarget(creature, profile));
        }

        if (profile.getAggressiveness() >= AIProfile.MIN_ACTIVE_TRAIT) {
            targets.add(new AggressivePlayerTarget(creature, profile));
        }

        return this;
    }

    /**
     * Adds a custom goal to the AI.
     */
    public AIBuilder withGoal(AIGoal goal) {
        goals.add(goal);
        return this;
    }

    /**
     * Adds a custom target to the AI.
     */
    public AIBuilder withTarget(AITarget target) {
        targets.add(target);
        return this;
    }

    /**
     * Applies the configured AI to the creature.
     */
    public void apply() {
        if (!goals.isEmpty() || !targets.isEmpty()) {
            AIEngine engine = new AIEngine(creature, goals, targets);
            creature.setAiEngine(engine);
        }
    }

    /**
     * Quick setup method - adds standard goals and targets, then applies.
     */
    public static void setupStandardAI(CustomCreature creature, AIProfile profile) {
        new AIBuilder(creature, profile)
                .withStandardGoals()
                .withStandardTargets()
                .apply();
    }
}
