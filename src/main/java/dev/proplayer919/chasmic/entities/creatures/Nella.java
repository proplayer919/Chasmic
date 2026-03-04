package dev.proplayer919.chasmic.entities.creatures;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.entities.CreatureFamily;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.goal.RandomStrollGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;
import net.minestom.server.utils.time.TimeUnit;

import java.util.List;
import java.util.UUID;

public class Nella extends CustomCreature {
    public Nella() {
        super(Main.getCreatureTypeRegistry().getCreatureType("nella"));

        getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(UUID.randomUUID().toString(), 5.0, AttributeOperation.ADD_VALUE));

        addAIGroup(
                List.of(
                        new MeleeAttackGoal(this, 1.6, 20, TimeUnit.SERVER_TICK), // Attack the target
                        new RandomStrollGoal(this, 20) // Walk around
                ),
                List.of(
                        new LastEntityDamagerTarget(this, 32), // First target the last entity which attacked you
                        new ClosestEntityTarget(this, 32, entity ->
                                entity instanceof CustomPlayer player &&
                                player.getGameMode() != GameMode.CREATIVE &&
                                player.getGameMode() != GameMode.SPECTATOR
                        ) // If there is none, target the nearest player (not in creative/spectator)
                )
        );
    }
}