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
            AttackResult attackResult = CombatUtils.calculateAttack(baseDamage, creature);

            // Calculate final damage (rounded)
            int finalDamage = Math.round(attackResult.getDamage());

            // Apply damage to target
            healthCreature.damage(finalDamage, DamageType.MOB_ATTACK, entity, target.getPosition());

            // Apply knockback to player (now with proper validation)
            if (target instanceof CustomPlayer player) {
                applyKnockback(player, creature);
                // playAttackSound(creature, player);
            }
        });
    }

    /**
     * Apply knockback to the player based on attack direction
     */
    private void applyKnockback(CustomPlayer player, CustomCreature creature) {
        try {
            // Get direction from creature to player
            Vec creaturePos = creature.getPosition().asVec();
            Vec playerPos = player.getPosition().asVec();
            Vec direction = playerPos.sub(creaturePos);

            // Check if direction is valid (not zero vector)
            if (direction.lengthSquared() < 0.01) {
                return; // Creature and player at same position, skip knockback
            }

            direction = direction.normalize();

            // Apply knockback (0.3 blocks horizontal, 0.2 vertical)
            double knockbackX = direction.x() * 0.3;
            double knockbackZ = direction.z() * 0.3;
            double knockbackY = 0.2;

            Vec knockbackVelocity = new Vec(knockbackX, knockbackY, knockbackZ);

            player.setVelocity(knockbackVelocity);
        } catch (Exception e) {
            // Silently fail on any knockback calculation errors
        }
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

