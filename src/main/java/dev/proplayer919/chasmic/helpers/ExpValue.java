package dev.proplayer919.chasmic.helpers;

import lombok.Getter;

@Getter
public class ExpValue {
    private final long exp;
    private final int level;
    private final long expToNextLevel;
    private final long expToPreviousLevel;

    public static final int MAX_LEVEL = 500;

    public ExpValue(long exp) {
        this.exp = exp;

        // Calculate level based on EXP
        this.level = calculateLevel(exp);

        // Calculate EXP needed for next and previous levels
        this.expToNextLevel = calculateExpToNextLevel(level);
        this.expToPreviousLevel = calculateExpToPreviousLevel(level);
    }

    private long calculateTotalExpForLevel(int level) {
        long totalExp = 0;
        for (int i = 1; i < level; i++) {
            totalExp += (long) (100 * Math.pow(1.15, i - 1)); // Total EXP required to reach the given level
        }
        return totalExp;
    }

    private int calculateLevel(long exp) {
        int level = 1;
        while (level < MAX_LEVEL && exp >= calculateTotalExpForLevel(level + 1)) {
            level++;
        }
        return level;
    }

    private long calculateExpToNextLevel(int currentLevel) {
        if (currentLevel == 0) return 100; // Level 1 requires 100 EXP
        if (currentLevel >= MAX_LEVEL) return 0; // Max level reached, no next level
        return calculateTotalExpForLevel(currentLevel + 1) - calculateTotalExpForLevel(currentLevel); // EXP needed to reach the next level
    }

    private long calculateExpToPreviousLevel(int currentLevel) {
        if (currentLevel <= 1) return 0; // Level 1 has no previous level
        return calculateTotalExpForLevel(currentLevel) - calculateTotalExpForLevel(currentLevel - 1); // EXP needed to reach the previous level
    }
}
