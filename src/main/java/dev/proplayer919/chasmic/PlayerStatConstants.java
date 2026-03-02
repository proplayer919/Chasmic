package dev.proplayer919.chasmic;

public abstract class PlayerStatConstants {
    private final static float BASE_ATTACK = 1.0f; // Base attack bonus
    private final static float BASE_DEFENSE = 0.0f; // Base defense reduction
    private final static float BASE_CRITICAL_CHANCE = 1.0f; // Base critical chance (1%)
    private final static float BASE_FARMING = 1.0f; // Base farming multiplier

    public static float getBaseValue(PlayerStat stat) {
        return switch (stat) {
            case ATTACK -> BASE_ATTACK;
            case DEFENSE -> BASE_DEFENSE;
            case CRITICAL_CHANCE -> BASE_CRITICAL_CHANCE;
            case FARMING -> BASE_FARMING;
        };
    }
}
