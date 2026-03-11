package dev.proplayer919.chasmic.helpers;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

@Getter
public class ExpValue {
    private final BigInteger exp;
    private final int level;
    private final BigInteger expToNextLevel;
    private final BigInteger expToPreviousLevel;

    public static final int MAX_LEVEL = 250;

    public ExpValue(BigInteger exp) {
        this.exp = exp;

        // Calculate level based on EXP
        this.level = calculateLevel(exp);

        // Calculate EXP needed for next and previous levels
        this.expToNextLevel = calculateExpToNextLevel(level);
        this.expToPreviousLevel = calculateExpToPreviousLevel(level);
    }

    public static BigInteger calculateTotalExpForLevel(int level) {
        BigInteger total = BigInteger.ZERO;

        BigDecimal base = new BigDecimal("1.05"); // 5% increase per level
        BigDecimal expForThisLevel = new BigDecimal("100"); // i=1 term

        for (int i = 1; i < level; i++) {
            BigInteger term = expForThisLevel.setScale(0, RoundingMode.DOWN).toBigIntegerExact();
            total = total.add(term);

            expForThisLevel = expForThisLevel.multiply(base); // next level term
        }
        return total;
    }

    private int calculateLevel(BigInteger exp) {
        int level = 1;
        while (level < MAX_LEVEL && exp.compareTo(calculateTotalExpForLevel(level + 1)) >= 0) {
            level++;
        }
        return level;
    }

    private BigInteger calculateExpToNextLevel(int currentLevel) {
        if (currentLevel == 0) return new BigInteger("100"); // Level 1 requires 100 EXP
        if (currentLevel >= MAX_LEVEL) return BigInteger.ZERO; // Max level reached, no next level
        return calculateTotalExpForLevel(currentLevel + 1).subtract(calculateTotalExpForLevel(currentLevel)); // EXP needed to reach the next level
    }

    private BigInteger calculateExpToPreviousLevel(int currentLevel) {
        if (currentLevel <= 1) return BigInteger.ZERO; // Level 1 has no previous level
        return calculateTotalExpForLevel(currentLevel).subtract(calculateTotalExpForLevel(currentLevel - 1)); // EXP needed to reach the previous level
    }
}
