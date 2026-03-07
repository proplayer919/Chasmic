package dev.proplayer919.chasmic.ai;

import dev.proplayer919.chasmic.ai.goal.AIGoal;
import dev.proplayer919.chasmic.ai.target.AITarget;
import dev.proplayer919.chasmic.entities.CustomCreature;
import lombok.Getter;
import net.minestom.server.entity.Entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The AI engine that manages and executes goals and targets for a creature.
 * This runs every tick to evaluate and execute the creature's behavior.
 */
@Getter
public class AIEngine {
    private final CustomCreature creature;
    private final List<AIGoal> goals;
    private final List<AITarget> targets;
    private AIGoal currentGoal;

    public AIEngine(CustomCreature creature, List<AIGoal> goals, List<AITarget> targets) {
        this.creature = creature;
        // Sort by priority (lower number = higher priority)
        this.goals = new ArrayList<>(goals);
        this.goals.sort(Comparator.comparingInt(AIGoal::getPriority));

        this.targets = new ArrayList<>(targets);
        this.targets.sort(Comparator.comparingInt(AITarget::getPriority));
    }

    /**
     * Tick the AI system - should be called every server tick.
     */
    public void tick() {
        // Don't tick if creature is not in an instance yet
        if (creature.getInstance() == null) {
            return;
        }

        // Update targeting
        updateTarget();

        // Update current goal
        updateGoals();

        // Tick current goal if active
        if (currentGoal != null && currentGoal.isActive()) {
            currentGoal.tick();

            // Check if goal should end
            if (currentGoal.shouldEnd()) {
                currentGoal.end();
                currentGoal = null;
            }
        }
    }

    /**
     * Update the creature's target based on target selectors.
     */
    private void updateTarget() {
        Entity currentTarget = creature.getTarget();

        // Eagerly clear stale/invalid targets before any new selection happens.
        if (currentTarget != null && !isValidForAnySelector(currentTarget)) {
            creature.setTarget(null);
            currentTarget = null;
        }

        // Select from highest-priority selector first.
        for (AITarget targetSelector : targets) {
            Entity newTarget = targetSelector.findTarget();
            if (newTarget == null) {
                continue;
            }

            if (currentTarget == null) {
                creature.setTarget(newTarget);
                return;
            }

            // Keep stable target when already valid for this selector.
            if (newTarget == currentTarget || targetSelector.isValidTarget(currentTarget)) {
                return;
            }

            creature.setTarget(newTarget);
            return;
        }
    }

    private boolean isValidForAnySelector(Entity target) {
        for (AITarget selector : targets) {
            if (selector.isValidTarget(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Update the active goal based on priorities and conditions.
     */
    private void updateGoals() {
        // If current goal is still valid, keep it
        if (currentGoal != null && currentGoal.isActive() && !currentGoal.shouldEnd()) {
            return;
        }

        // End current goal if it exists
        if (currentGoal != null && currentGoal.isActive()) {
            currentGoal.end();
        }

        // Find the highest priority goal that can start
        for (AIGoal goal : goals) {
            if (goal.canStart()) {
                currentGoal = goal;
                currentGoal.start();
                return;
            }
        }

        // No goal to execute
        currentGoal = null;
    }
}

