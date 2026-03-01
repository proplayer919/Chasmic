package dev.proplayer919.chasmic.entities;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.registry.RegistryKey;

public interface HealthCreature {
    int customHealth = 100;
    int customMaxHealth = 100;

    void damage(int amount, RegistryKey<DamageType> damageType, Entity attacker, Pos damageSourcePos);

    /**
     * Get the attack stat for this creature (damage bonus)
     * Default: 0.0f
     */
    default float getAttackStat() {
        return 0.0f;
    }

    /**
     * Get the defense stat for this creature (damage reduction 0-1)
     * Default: 0.0f
     */
    default float getDefenseStat() {
        return 0.0f;
    }

    /**
     * Get the critical chance stat for this creature (0-1)
     * Default: 0.0f
     */
    default float getCriticalChanceStat() {
        return 0.0f;
    }
}


