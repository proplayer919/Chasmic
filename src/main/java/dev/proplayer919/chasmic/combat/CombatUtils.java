package dev.proplayer919.chasmic.combat;

import dev.proplayer919.chasmic.entities.HealthCreature;

public class CombatUtils {
    public static AttackResult calculateAttack(float baseDamage, HealthCreature attacker) {
        float finalDamage = baseDamage;
        boolean isCritical = false;

        // Apply attack stat multiplier
        float attackStat = attacker.getAttackStat();
        finalDamage *= attackStat;

        // Check for critical hit using creature's critical chance stat
        float criticalChance = attacker.getCriticalChanceStat();
        if (Math.random() < criticalChance) {
            // Critical hit! Double the damage
            isCritical = true;
            finalDamage *= 2.0f;
        }

        return new AttackResult(finalDamage, isCritical);
    }

    public static int applyDefenseToIncomingDamage(int incomingDamage, float defense) {
        // Defense stat reduces damage by the specified percentage
        // defense = 0.5 means 50% damage reduction
        float damageReduction = incomingDamage * defense;
        int finalDamage = Math.round(incomingDamage - damageReduction);

        return Math.max(1, finalDamage); // Minimum 1 damage always gets through
    }
}
