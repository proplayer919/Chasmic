package dev.proplayer919.chasmic.entities.ai.goal;

import dev.proplayer919.chasmic.entities.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.pathfinding.Navigator;

/**
 * An intelligent melee attack goal that respects the creature's AI profile.
 * Adjusts attack behavior based on aggression, shyness, and other traits.
 */
public class SmartMeleeAttackGoal implements AIGoal {
    private final CustomCreature creature;
    private final AIProfile profile;
    private final Navigator navigator;
    private final long cooldown;
    private long lastAttackTime = 0;
    private boolean active = false;
    private long targetOutOfRangeStartTime = 0; // Track when target went out of range
    private static final long TARGET_TIMEOUT = 15000; // 15 seconds in milliseconds

    public SmartMeleeAttackGoal(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
        this.navigator = creature.getNavigator();
        this.cooldown = profile.getAttackCooldown() * 50L; // Convert ticks to milliseconds
    }

    @Override
    public boolean canStart() {
        if (!AIBehaviorRules.isTraitActive(profile.getAggressiveness())) {
            return false;
        }

        Entity target = creature.getTarget();
        if (target == null || target.isRemoved()) {
            return false;
        }

        if (AIBehaviorRules.shouldAvoidCombat(creature, profile)) {
            return false; // Too scared to attack
        }

        // Low-aggression creatures engage only when target is closer.
        double aggressionFactor = 0.4 + (profile.getAggressiveness() * 0.6);
        double engageRange = profile.getDetectionRange() * aggressionFactor;
        double distance = creature.getPosition().distance(target.getPosition());
        return distance <= engageRange;
    }

    @Override
    public void start() {
        active = true;
        targetOutOfRangeStartTime = 0; // Reset timeout timer
    }

    @Override
    public void tick() {
        if (!active) return;

        Entity target = creature.getTarget();
        if (target == null) {
            return;
        }

        // Face the target
        creature.lookAt(target);

        // Check distance to target
        double distance = creature.getPosition().distance(target.getPosition());

        // Track if target is out of detection range
        if (distance > profile.getDetectionRange()) {
            // Target is out of range - start or continue timeout
            if (targetOutOfRangeStartTime == 0) {
                targetOutOfRangeStartTime = System.currentTimeMillis();
            }
            // Still try to move toward target while within timeout period
            navigator.setPathTo(target.getPosition());
        } else {
            // Target is within detection range - reset timeout
            targetOutOfRangeStartTime = 0;

            if (distance > profile.getAttackRange()) {
                // Too far to attack, move closer
                navigator.setPathTo(target.getPosition());
            } else {
                // In range, try to attack
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAttackTime >= cooldown) {
                    // Attack!
                    creature.attack(target, true);
                    lastAttackTime = currentTime;
                }
            }
        }
    }

    @Override
    public boolean shouldEnd() {
        Entity target = creature.getTarget();
        if (target == null || target.isRemoved()) {
            return true;
        }

        if (AIBehaviorRules.shouldAvoidCombat(creature, profile)) {
            return true; // Stop attacking and flee
        }

        double distance = creature.getPosition().distance(target.getPosition());

        // If target is out of detection range, check timeout
        if (distance > profile.getDetectionRange()) {
            // If we've been out of range for more than 15 seconds, give up
            if (targetOutOfRangeStartTime > 0) {
                long timeOutOfRange = System.currentTimeMillis() - targetOutOfRangeStartTime;
                return timeOutOfRange > TARGET_TIMEOUT; // Timeout reached, give up on this target
            }
            // Still within timeout, keep pursuing
            return false;
        }

        // Target is within range, continue
        return false;
    }

    @Override
    public void end() {
        active = false;
    }

    @Override
    public int getPriority() {
        return 2; // Medium-high priority
    }

    @Override
    public boolean isActive() {
        return active;
    }
}

