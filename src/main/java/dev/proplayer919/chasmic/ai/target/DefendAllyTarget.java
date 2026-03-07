package dev.proplayer919.chasmic.ai.target;

import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.Entity;

/**
 * Targets enemies that are attacking allies.
 * Only loyal creatures will help their allies in combat.
 */
public class DefendAllyTarget implements AITarget {
    private final CustomCreature creature;
    private final AIProfile profile;

    public DefendAllyTarget(CustomCreature creature, AIProfile profile) {
        this.creature = creature;
        this.profile = profile;
    }

    @Override
    public Entity findTarget() {
        // Safety check - don't run if not in an instance
        if (creature.getInstance() == null) {
            return null;
        }

        // Only loyal creatures defend allies
        if (profile.getLoyalty() < 0.5f) {
            return null;
        }

        // Don't help allies if too shy
        if (profile.getShyness() > 0.5f) {
            return null;
        }

        // Find nearby allies of the same type
        return creature.getInstance()
                .getNearbyEntities(creature.getPosition(), profile.getDetectionRange())
                .stream()
                .filter(e -> e instanceof CustomCreature)
                .filter(e -> e != creature)
                .filter(e -> {
                    CustomCreature ally = (CustomCreature) e;
                    // Check if same type
                    if (!ally.getCreatureType().id().equals(creature.getCreatureType().id())) {
                        return false;
                    }
                    // Check if ally has an attacker
                    return ally.getLastAttacker() != null && !ally.getLastAttacker().isRemoved();
                })
                .map(e -> ((CustomCreature) e).getLastAttacker())
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getPriority() {
        return 2; // High priority - defending allies
    }
}


