package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.combat.AttackResult;
import dev.proplayer919.chasmic.combat.CombatUtils;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import org.jspecify.annotations.NonNull;

/**
 * Module that handles players attacking custom creatures with custom damage logic.
 * Players can attack creatures with melee weapons (held items) or spells.
 */
public class PlayerAttackModule implements Module {

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(EntityAttackEvent.class, event -> {
            // Only handle CustomPlayer attacking CustomCreature
            if (!(event.getEntity() instanceof CustomPlayer player)) {
                return; // Not a custom player, ignore
            }

            if (!(event.getTarget() instanceof CustomCreature creature)) {
                return; // Target is not a custom creature, ignore
            }

            // Apply player stats and check for critical hit
            AttackResult attackResult = CombatUtils.calculateAttack(player, creature);

            // Calculate final damage (rounded)
            int finalDamage = Math.round(attackResult.getDamage());

            // Apply damage to the creature
            creature.damage(finalDamage, DamageType.PLAYER_ATTACK, player, player.getPosition());

            // Set the creature's last attacker
            creature.setLastAttacker(player);

            // Play attack sound (with critical hit sound if applicable)
            playAttackSound(player, creature, attackResult);
        });
    }

    /**
     * Play attack sound for the player attacking
     * Plays a special sound effect when the attack is a critical hit
     */
    private void playAttackSound(CustomPlayer player, CustomCreature creature, AttackResult attackResult) {
        if (attackResult.isCritical()) {
            // Play critical hit sound - a higher pitched and more impactful sound
            player.playSound(Sound.sound(
                    Key.key("minecraft:entity.player.attack.crit"),
                    Sound.Source.PLAYER,
                    1.0f,
                    1.0f
            ));
        } else {
            // Play normal sword hit sound to player
            player.playSound(Sound.sound(
                    Key.key("minecraft:entity.player.attack.sweep"),
                    Sound.Source.PLAYER,
                    0.5f,
                    1.0f
            ));
        }
    }

    @Override
    public String getName() {
        return "PlayerAttackModule";
    }
}

