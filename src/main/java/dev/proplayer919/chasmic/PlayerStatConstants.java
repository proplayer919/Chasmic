package dev.proplayer919.chasmic;

public class PlayerStatConstants {
    private final static float BASE_ATTACK = 1.0f; // Base attack multiplier
    private final static float BASE_DEFENSE = 0.0f; // Base defense reduction
    private final static float BASE_CRITICAL_CHANCE = 0.05f; // Base critical chance (5%)

    public static float getBaseValue(PlayerStat stat) {
        return switch (stat) {
            case ATTACK -> BASE_ATTACK;
            case DEFENSE -> BASE_DEFENSE;
            case CRITICAL_CHANCE -> BASE_CRITICAL_CHANCE;
        };
    }
}
