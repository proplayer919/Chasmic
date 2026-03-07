package dev.proplayer919.chasmic.ai;

import dev.proplayer919.chasmic.ai.goal.*;
import dev.proplayer919.chasmic.ai.target.AggressivePlayerTarget;
import dev.proplayer919.chasmic.ai.target.DefendAllyTarget;
import dev.proplayer919.chasmic.ai.target.AITarget;
import dev.proplayer919.chasmic.ai.target.SmartLastDamagerTarget;
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
        // Flee goal (highest priority for shy creatures)
        if (profile.getShyness() > 0.3f) {
            goals.add(new FleeGoal(creature, profile));
        }

        // Combat goal
        if (profile.getAggressiveness() > 0.0f) {
            goals.add(new SmartMeleeAttackGoal(creature, profile));
        }

        // Social goals
        if (profile.getSociability() > 0.3f) {
            goals.add(new StayNearAlliesGoal(creature, profile));
        }

        // Investigation goal for curious creatures
        if (profile.getCuriosity() > 0.3f) {
            goals.add(new InvestigateGoal(creature, profile));
        }

        // Wandering goal (lowest priority)
        if (profile.getWanderlust() > 0.0f) {
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

        // Defend allies if loyal
        if (profile.getLoyalty() > 0.5f) {
            targets.add(new DefendAllyTarget(creature, profile));
        }

        // Aggressive hunting
        if (profile.getAggressiveness() > 0.5f) {
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

