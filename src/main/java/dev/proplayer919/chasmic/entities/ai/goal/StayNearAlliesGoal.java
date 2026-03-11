package dev.proplayer919.chasmic.entities.ai.goal;

import dev.proplayer919.chasmic.entities.ai.AIBehaviorRules;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.pathfinding.Navigator;

/**
 * Makes creatures follow and stay near similar creatures (same type).
 * Higher sociability = stronger tendency to group up.
 */
public class StayNearAlliesGoal implements AIGoal {
    private final CustomCreature creature;
    private final AIProfile profile;
    private final Navigator navigator;
    private CustomCreature allyToFollow;
    private static final double DESIRED_DISTANCE = 5.0;
    private static final double MAX_DISTANCE = 15.0;
    private static final double SEARCH_RANGE = 25.0;
    private static final double SOCIALIZE_BASE_CHANCE_PER_TICK = 0.05; // 0-5% per tick based on sociability
    private boolean active = false;

    public StayNearAlliesGoal(CustomCreature creature, AIProfile profile) {
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

        if (!AIBehaviorRules.isTraitActive(profile.getSociability())) {
            return false;
        }

        // Don't group during combat
        if (creature.getTarget() != null) {
            return false;
        }

        if (!AIBehaviorRules.rollTraitChance(profile.getSociability(), SOCIALIZE_BASE_CHANCE_PER_TICK)) {
            return false;
        }

        // Find nearby allies of the same type
        allyToFollow = (CustomCreature) creature.getInstance()
                .getNearbyEntities(creature.getPosition(), SEARCH_RANGE)
                .stream()
                .filter(e -> e instanceof CustomCreature)
                .filter(e -> e != creature)
                .filter(e -> {
                    CustomCreature other = (CustomCreature) e;
                    return other.getCreatureType().id().equals(creature.getCreatureType().id());
                })
                .findFirst()
                .orElse(null);

        return allyToFollow != null;
    }

    @Override
    public void start() {
        active = true;
    }

    @Override
    public void tick() {
        if (!active) return;

        if (allyToFollow == null || allyToFollow.isRemoved()) {
            return;
        }

        double distance = creature.getPosition().distance(allyToFollow.getPosition());

        if (distance > DESIRED_DISTANCE * 1.5) {
            // Too far, move closer
            navigator.setPathTo(allyToFollow.getPosition());
        } else if (distance < DESIRED_DISTANCE * 0.5) {
            // Too close, back off a bit
            var creaturePos = creature.getPosition();
            var allyPos = allyToFollow.getPosition();

            double dx = creaturePos.x() - allyPos.x();
            double dz = creaturePos.z() - allyPos.z();
            double length = Math.sqrt(dx * dx + dz * dz);

            if (length > 0) {
                dx = (dx / length) * DESIRED_DISTANCE;
                dz = (dz / length) * DESIRED_DISTANCE;
            }

            navigator.setPathTo(creaturePos.add(dx, 0, dz));
        }
        // Otherwise, stay put (within desired range)
    }

    @Override
    public boolean shouldEnd() {
        if (allyToFollow == null || allyToFollow.isRemoved()) {
            return true;
        }

        // Stop if ally is too far away
        double distance = creature.getPosition().distance(allyToFollow.getPosition());
        return distance > MAX_DISTANCE;
    }

    @Override
    public void end() {
        allyToFollow = null;
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

