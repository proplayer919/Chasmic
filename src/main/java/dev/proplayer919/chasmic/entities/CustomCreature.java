package dev.proplayer919.chasmic.entities;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

import java.util.Date;

@Getter
public class CustomCreature extends EntityCreature implements HealthCreature {
    @Setter
    private int customHealth;

    private final CreatureType creatureType;

    private Entity lastAttacker;
    private Date lastDamageTime;
    private int lastDisplayedHealth = -1; // Cache for health display to prevent unnecessary updates

    public CustomCreature(CreatureType creatureType) {
        super(creatureType.entityType());
        this.creatureType = creatureType;
        this.customHealth = creatureType.maxHealth();
    }

    @Override
    public float getAttackStat() {
        return creatureType.attack();
    }

    @Override
    public float getDefenseStat() {
        return creatureType.defense();
    }

    @Override
    public float getCriticalChanceStat() {
        return creatureType.criticalChance();
    }

    @Override
    public float getSpeedStat() {
        return creatureType.speed();
    }

    public void setLastAttacker(Entity attacker) {
        this.lastAttacker = attacker;
        this.lastDamageTime = new Date();
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        // If the creature has taken damage, and it's been more than 5 seconds since the last damage, reset the health display
        if (lastDamageTime != null && new Date().getTime() - lastDamageTime.getTime() > 5000) {
            lastDamageTime = null;
            lastAttacker = null;
            updateHealthDisplay();
        }

        // Update movement speed based on speed stat
        float speedBonus = getSpeedStat();
        float calculatedSpeed = 0.1f * (1 + speedBonus / 100);
        getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(speedBonus != 0 ? calculatedSpeed : 0.1f);
    }

    /**
     * Updates the custom name to display health if the creature is not at max health
     * Only calls setCustomName if health actually changed to prevent lag
     */
    private void updateHealthDisplay() {
        boolean shouldShowHealth = customHealth < customMaxHealth && customHealth > 0 && lastAttacker != null;

        // Only update if health display state changed
        if (shouldShowHealth) {
            // Only update if health value actually changed
            if (lastDisplayedHealth != customHealth) {
                Component healthDisplay = Component.text("❤ " + customHealth + "/" + customMaxHealth)
                        .color(NamedTextColor.RED);
                setCustomName(healthDisplay);
                setCustomNameVisible(true);
                lastDisplayedHealth = customHealth;
            }
        } else {
            setCustomNameVisible(false);
        }
    }

    @Override
    public void damage(int amount, RegistryKey<DamageType> damageType, Entity attacker, Pos damageSourcePos) {
        this.customHealth -= amount;
        if (attacker != null) {
            setLastAttacker(attacker);
        }

        // Update the health display
        updateHealthDisplay();

        if (this.customHealth <= 0) {
            this.customHealth = 0;
            this.kill();
        }
    }
}
