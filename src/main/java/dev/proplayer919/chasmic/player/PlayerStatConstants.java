package dev.proplayer919.chasmic.player;

public abstract class PlayerStatConstants {
    private final static float BASE_ATTACK = 1.0f; // Base attack bonus
    private final static float BASE_DEFENSE = 0.0f; // Base defense reduction
    private final static float BASE_CRITICAL_CHANCE = 1.0f; // Base critical chance (1%)
    private final static float BASE_SPEED = 0.0f; // Base speed bonus
    private final static float BASE_FARMING = 1.0f; // Base farming multiplier
    private final static float BASE_INTELLIGENCE = 100.0f; // Base intelligence (max mana)
    private final static float BASE_HEALTH = 100.0f; // Base max health

    public static float getBaseValue(PlayerStat stat) {
        return switch (stat) {
            case ATTACK -> BASE_ATTACK;
            case DEFENSE -> BASE_DEFENSE;
            case CRITICAL_CHANCE -> BASE_CRITICAL_CHANCE;
            case SPEED -> BASE_SPEED;
            case FARMING -> BASE_FARMING;
            case INTELLIGENCE -> BASE_INTELLIGENCE;
            case HEALTH -> BASE_HEALTH;
        };
    }
}
