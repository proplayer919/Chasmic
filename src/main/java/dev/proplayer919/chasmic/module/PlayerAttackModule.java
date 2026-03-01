package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.combat.AttackResult;
import dev.proplayer919.chasmic.combat.CombatUtils;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityAttackEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles players attacking custom creatures with custom damage logic.
 * Players can attack creatures with melee weapons (held items) or spells.
 */
public class PlayerAttackModule implements Module {

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(EntityAttackEvent.class, event -> {
            // Only handle CustomPlayer attacking CustomCreature
            if (!(event.getEntity() instanceof CustomPlayer player)) {
                return; // Not a custom player, ignore
            }

            if (!(event.getTarget() instanceof CustomCreature creature)) {
                return; // Target is not a custom creature, ignore
            }

            // Get the base damage from the player's held item
            float baseDamage = player.getAttackStat();

            // Apply player stats and check for critical hit
            AttackResult attackResult = CombatUtils.calculateAttack(baseDamage, player, creature);

            // Calculate final damage (rounded)
            int finalDamage = Math.round(attackResult.getDamage());

            // Apply damage to the creature
            creature.damage(finalDamage, DamageType.PLAYER_ATTACK, player, player.getPosition());

            // Set the creature's last attacker
            creature.setLastAttacker(player);

            // Apply knockback
            applyKnockback(creature, player);

            // Play attack sound (with critical hit sound if applicable)
            playAttackSound(player, creature, attackResult);
        });
    }

    /**
     * Apply knockback to the creature based on attack direction
     */
    private void applyKnockback(CustomCreature creature, CustomPlayer player) {
        try {
            // Get direction from player to creature
            Vec playerPos = player.getPosition().asVec();
            Vec creaturePos = creature.getPosition().asVec();
            Vec direction = creaturePos.sub(playerPos);

            // Check if direction is valid (not zero vector)
            if (direction.lengthSquared() < 0.01) {
                return; // Player and creature at same position, skip knockback
            }

            direction = direction.normalize();

            // Apply knockback (1 block horizontal, 0.6 vertical)
            double knockbackX = direction.x();
            double knockbackZ = direction.z();
            double knockbackY = 0.6;

            Vec knockbackVelocity = new Vec(knockbackX, knockbackY, knockbackZ);

            creature.setVelocity(knockbackVelocity);
        } catch (Exception e) {
            // Silently fail on any knockback calculation errors
        }
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

