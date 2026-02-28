package dev.proplayer919.chasmic.entities;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;

@Getter
public class CustomCreature extends EntityCreature {
    @Setter
    private int customHealth = 100;

    @Setter
    private int customMaxHealth = 100;

    private final CreatureType creatureType;

    public CustomCreature(EntityType entityType, int maxHealth, CreatureType creatureType) {
        super(entityType);
        this.customMaxHealth = maxHealth;
        this.customHealth = maxHealth;
        this.creatureType = creatureType;
    }

    public void damage(int amount) {
        this.customHealth -= amount;
        if (this.customHealth <= 0) {
            this.customHealth = 0;
            this.kill();
        }
    }
}
