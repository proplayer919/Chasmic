package dev.proplayer919.chasmic.entities.ai.goal;

import dev.proplayer919.chasmic.entities.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.pathfinding.Navigator;

/**
 * Makes curious creatures investigate nearby players and items without attacking.
 * The creature will approach and circle around the object of interest.
 */
public class InvestigateGoal implements AIGoal {
    private final CustomCreature creature;
    private final AIProfile profile;
    private final Navigator navigator;
    private Entity targetOfInterest;
    private long investigationStartTime;
    private static final long MAX_INVESTIGATION_TIME = 10000; // 10 seconds
    private static final double INVESTIGATION_RANGE = 16.0;
    private static final double APPROACH_DISTANCE = 3.0;
    private static final double INVESTIGATE_BASE_CHANCE_PER_TICK = 0.01; // 0-1% per tick based on curiosity
    private boolean active = false;

    public InvestigateGoal(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
        this.navigator = creature.getNavigator();
    }

    @Override
    public boolean canStart() {
        // Safety check - don't run if not in an instance
        if (creature.getInstance() == null) {
            return false;
        }

        // Only active when trait is enabled
        if (!AIBehaviorRules.isTraitActive(profile.getCuriosity())) {
            return false;
        }

        // Don't investigate if in combat
        if (creature.getTarget() != null) {
            return false;
        }

        if (!AIBehaviorRules.rollTraitChance(profile.getCuriosity(), INVESTIGATE_BASE_CHANCE_PER_TICK)) {
            return false;
        }

        // Find nearby players to investigate
        targetOfInterest = creature.getInstance()
                .getNearbyEntities(creature.getPosition(), INVESTIGATION_RANGE)
                .stream()
                .filter(e -> e instanceof Player)
                .findFirst()
                .orElse(null);

        return targetOfInterest != null;
    }

    @Override
    public void start() {
        investigationStartTime = System.currentTimeMillis();
        active = true;
    }

    @Override
    public void tick() {
        if (!active) return;

        if (targetOfInterest == null || targetOfInterest.isRemoved()) {
            return;
        }

        // Look at the target
        creature.lookAt(targetOfInterest);

        double distance = creature.getPosition().distance(targetOfInterest.getPosition());

        if (distance > APPROACH_DISTANCE) {
            // Approach the target
            navigator.setPathTo(targetOfInterest.getPosition());
        } else {
            // Circle around the target
            var creaturePos = creature.getPosition();
            var targetPos = targetOfInterest.getPosition();

            // Calculate a position to the side
            double angle = Math.toRadians(creature.getPosition().yaw() + 45);
            double x = targetPos.x() + Math.cos(angle) * APPROACH_DISTANCE;
            double z = targetPos.z() + Math.sin(angle) * APPROACH_DISTANCE;

            navigator.setPathTo(creaturePos.withX(x).withZ(z));
        }
    }

    @Override
    public boolean shouldEnd() {
        if (targetOfInterest == null || targetOfInterest.isRemoved()) {
            return true;
        }

        // Stop investigating if too much time passed
        if (System.currentTimeMillis() - investigationStartTime > MAX_INVESTIGATION_TIME) {
            return true;
        }

        // Stop if target moved too far away
        double distance = creature.getPosition().distance(targetOfInterest.getPosition());
        return distance > INVESTIGATION_RANGE * 1.5;
    }

    @Override
    public void end() {
        targetOfInterest = null;
        active = false;
    }

    @Override
    public int getPriority() {
        return 3; // Medium priority
    }

    @Override
    public boolean isActive() {
        return active;
    }
}

