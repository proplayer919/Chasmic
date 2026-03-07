package dev.proplayer919.chasmic.ai.target;

import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.entities.CustomCreature;
import dev.proplayer919.chasmic.player.CustomPlayer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;

/**
 * Centralized target validation rules shared by selectors and the AI engine.
 */
public final class AITargetingRules {
    private AITargetingRules() {
    }

    public static boolean isCommonlyValidTarget(CustomCreature creature, Entity entity) {
        return entity != null
                && !entity.isRemoved()
                && creature.getInstance() != null
                && entity.getInstance() == creature.getInstance();
    }

    public static boolean isValidPlayerTarget(CustomCreature creature, AIProfile profile, Entity entity) {
        if (!(entity instanceof CustomPlayer player)) {
            return false;
        }
        if (!isCommonlyValidTarget(creature, entity)) {
            return false;
        }

        if (!profile.isTargetCreativePlayers()) {
            GameMode mode = player.getGameMode();
            return mode != GameMode.CREATIVE && mode != GameMode.SPECTATOR;
        }
        return true;
    }
}

