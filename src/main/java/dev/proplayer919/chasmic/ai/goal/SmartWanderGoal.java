package dev.proplayer919.chasmic.ai.goal;

import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.Navigator;

import java.util.concurrent.ThreadLocalRandom;

/**
 * An intelligent wander goal that respects the creature's wanderlust trait.
 * Higher wanderlust = more frequent and longer-distance wandering.
 */
public class SmartWanderGoal implements AIGoal {
    private final CustomCreature creature;
    private final AIProfile profile;
    private final Navigator navigator;
    private final int wanderRadius;
    private boolean active = false;

    public SmartWanderGoal(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
        this.navigator = creature.getNavigator();
        // Wanderlust affects how far the creature wanders
        // 0.0 = 5 blocks, 0.5 = 15 blocks, 1.0 = 25 blocks
        this.wanderRadius = (int) (5 + (profile.getWanderlust() * 20));
    }

    @Override
    public boolean canStart() {
        // Don't wander if creature has a target
        if (creature.getTarget() != null) {
            return false;
        }

        // Wanderlust affects frequency - less wanderlust = less likely to wander
        double chance = profile.getWanderlust() * 0.02; // 0-2% chance per tick
        return Math.random() < chance;
    }

    @Override
    public void start() {
        active = true;
        // Pick a random position to wander to
        Pos currentPos = creature.getPosition();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double offsetX = random.nextDouble(-wanderRadius, wanderRadius);
        double offsetZ = random.nextDouble(-wanderRadius, wanderRadius);

        Pos targetPos = currentPos.add(offsetX, 0, offsetZ);
        navigator.setPathTo(targetPos);
    }

    @Override
    public void tick() {
        // Navigation handles the movement
    }

    @Override
    public boolean shouldEnd() {
        // End if the creature has a target
        return creature.getTarget() != null;
    }

    @Override
    public void end() {
        active = false;
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

