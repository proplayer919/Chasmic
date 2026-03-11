package dev.proplayer919.chasmic.entities.ai.goal;

import dev.proplayer919.chasmic.entities.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.Navigator;

import java.util.concurrent.ThreadLocalRandom;

/**
 * An intelligent wander goal that respects the creature's wanderlust trait.
 * Higher wanderlust = more frequent and longer-distance wandering.
 */
public class SmartWanderGoal implements AIGoal {
    private static final double WANDER_BASE_CHANCE_PER_TICK = 0.02; // 0-2% per tick based on wanderlust
    private static final double ARRIVAL_DISTANCE = 1.25;
    private static final long MAX_WANDER_TIME_MS = 10000;

    private final CustomCreature creature;
    private final AIProfile profile;
    private final Navigator navigator;
    private final int wanderRadius;

    private boolean active = false;
    private Pos wanderTarget;
    private long wanderStartTime;

    public SmartWanderGoal(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
        this.navigator = creature.getNavigator();
        // 0.0 -> 5 blocks, 0.5 -> 15 blocks, 1.0 -> 25 blocks
        this.wanderRadius = (int) (5 + (profile.getWanderlust() * 20));
    }

    @Override
    public boolean canStart() {
        if (!AIBehaviorRules.isTraitActive(profile.getWanderlust())) {
            return false;
        }

        // Don't wander if creature has a target
        if (creature.getTarget() != null) {
            return false;
        }

        // Wanderlust affects frequency - less wanderlust = less likely to wander
        return AIBehaviorRules.rollTraitChance(profile.getWanderlust(), WANDER_BASE_CHANCE_PER_TICK);
    }

    @Override
    public void start() {
        active = true;
        wanderStartTime = System.currentTimeMillis();

        Pos currentPos = creature.getPosition();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double offsetX = random.nextDouble(-wanderRadius, wanderRadius);
        double offsetZ = random.nextDouble(-wanderRadius, wanderRadius);

        wanderTarget = currentPos.add(offsetX, 0, offsetZ);
        navigator.setPathTo(wanderTarget);
    }

    @Override
    public void tick() {
        // Navigation handles movement to wanderTarget.
    }

    @Override
    public boolean shouldEnd() {
        // End if the creature enters combat.
        if (creature.getTarget() != null) {
            return true;
        }

        if (wanderTarget == null) {
            return true;
        }

        // End once destination is reached so other low-priority goals can run.
        double distance = creature.getPosition().distance(wanderTarget);
        if (distance <= ARRIVAL_DISTANCE) {
            return true;
        }

        // Guard against rare pathing dead-ends.
        return System.currentTimeMillis() - wanderStartTime > MAX_WANDER_TIME_MS;
    }

    @Override
    public void end() {
        active = false;
        wanderTarget = null;
    }

    @Override
    public int getPriority() {
        return 4; // Lowest priority - idle behavior
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
