package dev.proplayer919.chasmic;

public class PlayerStatConstants {
    private final static float BASE_ATTACK = 1.0f; // Base attack multiplier
    private final static float BASE_DEFENSE = 0.0f; // Base defense reduction
    private final static float BASE_CRITICAL_CHANCE = 0.05f; // Base critical chance (5%)

    public static float getBaseValue(PlayerStat stat) {
        switch (stat) {
            case ATTACK:
                return BASE_ATTACK;
            case DEFENSE:
                return BASE_DEFENSE;
            case CRITICAL_CHANCE:
                return BASE_CRITICAL_CHANCE;
            default:
                throw new IllegalArgumentException("Unknown stat: " + stat);
        }
    }
}
