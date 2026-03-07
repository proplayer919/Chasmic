package dev.proplayer919.chasmic.ai.target;

import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import dev.proplayer919.chasmic.player.CustomPlayer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;

/**
 * Intelligently targets players based on the creature's aggression level.
 * More aggressive creatures will actively seek out players from farther away.
 */
public class AggressivePlayerTarget implements AITarget {
    private final CustomCreature creature;
    private final AIProfile profile;

    public AggressivePlayerTarget(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
    }

    @Override
    public Entity findTarget() {
        // Safety check - don't run if not in an instance
        if (creature.getInstance() == null) {
            return null;
        }

        // Only aggressive creatures actively seek targets
        if (profile.getAggressiveness() < 0.5f) {
            return null;
        }

        // Don't target if too shy and injured
        if (profile.getShyness() > 0.5f) {
            float healthPercent = (float) creature.getCustomHealth() / creature.getCreatureType().maxHealth();
            if (healthPercent < profile.getFleeHealthThreshold()) {
                return null; // Too scared to hunt
            }
        }

        // Adjust range based on aggression
        double searchRange = profile.getDetectionRange() * profile.getAggressiveness();

        return creature.getInstance()
                .getNearbyEntities(creature.getPosition(), searchRange)
                .stream()
                .filter(this::isValidTarget)
                .findFirst()
                .orElse(null);
    }

    private boolean isValidTarget(Entity entity) {
        if (!(entity instanceof CustomPlayer player)) {
            return false;
        }

        // Check if entity is removed
        if (entity.isRemoved()) {
            return false;
        }

        // Don't target creative/spectator players unless configured to do so
        if (!profile.isTargetCreativePlayers()) {
            GameMode mode = player.getGameMode();
            return mode != GameMode.CREATIVE && mode != GameMode.SPECTATOR;
        }

        return true;
    }

    @Override
    public int getPriority() {
        return 3; // Medium priority - aggressive hunting
    }
}

