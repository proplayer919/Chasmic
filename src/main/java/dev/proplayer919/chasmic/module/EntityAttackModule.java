package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.combat.AttackResult;
import dev.proplayer919.chasmic.combat.CombatUtils;
import dev.proplayer919.chasmic.entities.CustomCreature;
import dev.proplayer919.chasmic.entities.HealthCreature;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles entities attacking players and applying custom damage logic with critical hits
 */
public class EntityAttackModule implements Module {

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(EntityAttackEvent.class, event -> {
            Entity entity = event.getEntity();
            Entity target = event.getTarget();

            if (!(entity instanceof CustomCreature creature)) {
                return; // Not a custom creature, ignore
            }

            if (!(target instanceof HealthCreature healthCreature)) {
                return; // Target is not a HealthCreature, ignore
            }

            // Get base damage from creature
            int baseDamage = 1;

            // Apply creature attack stat and check for critical hit
            AttackResult attackResult = CombatUtils.calculateAttack(baseDamage, creature, healthCreature);

            // Calculate final damage (rounded)
            int finalDamage = Math.round(attackResult.getDamage());

            // Apply damage to target
            healthCreature.damage(finalDamage, DamageType.MOB_ATTACK, entity, target.getPosition());

            if (target instanceof CustomPlayer player) {
                playAttackSound(creature, player);
            }
        });
    }

    /**
     * Play attack sound for the creature attacking
     */
    private void playAttackSound(@SuppressWarnings("unused") CustomCreature creature, CustomPlayer player) {
        // Play hurt sound to player
        player.playSound(Sound.sound(
                net.kyori.adventure.key.Key.key("minecraft:entity.player.hurt"),
                Sound.Source.HOSTILE,
                1.0f,
                1.0f
        ));
    }

    @Override
    public String getName() {
        return "EntityAttackModule";
    }
}

