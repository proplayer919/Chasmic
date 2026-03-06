package dev.proplayer919.chasmic.combat;

import dev.proplayer919.chasmic.entities.HealthCreature;

public abstract class CombatUtils {
    public static AttackResult calculateAttack(HealthCreature attacker, HealthCreature target) {
        float finalDamage = 0f;
        boolean isCritical = false;

        // Apply attack stat bonus
        float attackStat = attacker.getAttackStat();
        finalDamage += attackStat;

        // Check for critical hit using creature's critical chance stat
        float criticalChance = attacker.getCriticalChanceStat() / 100;
        if (Math.random() < criticalChance) {
            // Critical hit! Double the damage
            isCritical = true;
            finalDamage *= 2.0f;
        }

        // Apply target's defense stat to reduce incoming damage
        float defenseStat = target.getDefenseStat();
        finalDamage = applyDefenseToIncomingDamage(Math.round(finalDamage), defenseStat);

        return new AttackResult(finalDamage, isCritical);
    }

    public static int applyDefenseToIncomingDamage(int incomingDamage, float defense) {
        // Defense stat reduces damage exponentially (range 0-∞)
        // For example, a defense of 500 would reduce damage to 50%, while a defense of 1000 would reduce it to 40%
        float damageMultiplier = 1 / (1 + (defense / 500.0f));
        return Math.round(incomingDamage * damageMultiplier);
    }
}
