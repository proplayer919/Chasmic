package dev.proplayer919.chasmic.entities;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

import java.util.Date;

@Getter
public class CustomCreature extends EntityCreature implements HealthCreature {
    @Setter
    private int customHealth;

    @Setter
    private int customMaxHealth;

    private final CreatureType creatureType;

    private float attack = 1.0f; // Flat attack damage
    private float defense = 0.0f; // Defense damage reduction (0-1, where 0.5 = 50% reduction)
    private float criticalChance = 0.0f; // Critical chance (0-1, where 0.1 = 10% chance)

    private final String id;
    private final String name;

    private Entity lastAttacker;
    private Date lastDamageTime;
    private int lastDisplayedHealth = -1; // Cache for health display to prevent unnecessary updates

    public CustomCreature(String id, String name, EntityType entityType, int maxHealth, CreatureType creatureType) {
        super(entityType);
        this.id = id;
        this.name = name;
        this.customMaxHealth = maxHealth;
        this.customHealth = maxHealth;
        this.creatureType = creatureType;
    }

    public CustomCreature(String id, String name, EntityType entityType, int maxHealth, CreatureType creatureType, float attack, float defense, float criticalChance) {
        this(id, name, entityType, maxHealth, creatureType);
        this.attack = attack;
        this.defense = defense;
        this.criticalChance = criticalChance;
    }

    @Override
    public float getAttackStat() {
        return attack;
    }

    @Override
    public float getDefenseStat() {
        return defense;
    }

    @Override
    public float getCriticalChanceStat() {
        return criticalChance;
    }

    public void setLastAttacker(Entity attacker) {
        this.lastAttacker = attacker;
        this.lastDamageTime = new Date();
    }

    /**
     * Updates the custom name to display health if the creature is not at max health
     * Only calls setCustomName if health actually changed to prevent lag
     */
    private void updateHealthDisplay() {
        boolean shouldShowHealth = customHealth < customMaxHealth && customHealth > 0;

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
