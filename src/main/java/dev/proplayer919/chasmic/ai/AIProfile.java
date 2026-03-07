package dev.proplayer919.chasmic.ai;

import lombok.Builder;
import lombok.Getter;

/**
 * Defines the AI personality and behavior traits for a creature.
 * All values range from 0.0 to 1.0 unless otherwise specified.
 */
@Getter
@Builder
public class AIProfile {
    /**
     * How aggressive the creature is toward players and other entities.
     * 0.0 = Passive (never attacks unless provoked)
     * 0.5 = Neutral (attacks when nearby or provoked)
     * 1.0 = Aggressive (actively hunts players)
     */
    @Builder.Default
    private final float aggressiveness = 0.5f;

    /**
     * How shy/timid the creature is.
     * 0.0 = Fearless (never runs away)
     * 0.5 = Cautious (runs when low health)
     * 1.0 = Extremely shy (runs from any threat)
     */
    @Builder.Default
    private final float shyness = 0.0f;

    /**
     * How curious the creature is about players and items.
     * 0.0 = Not curious at all
     * 0.5 = Moderately curious
     * 1.0 = Extremely curious (investigates everything)
     */
    @Builder.Default
    private final float curiosity = 0.0f;

    /**
     * How social the creature is (tendency to group with similar creatures).
     * 0.0 = Solitary
     * 0.5 = Sometimes groups
     * 1.0 = Always stays in groups
     */
    @Builder.Default
    private final float sociability = 0.0f;

    /**
     * How territorial the creature is.
     * 0.0 = Not territorial
     * 0.5 = Defends territory when threatened
     * 1.0 = Aggressively defends territory
     */
    @Builder.Default
    private final float territoriality = 0.0f;

    /**
     * How much the creature wanders around.
     * 0.0 = Stays in one place
     * 0.5 = Moderate wandering
     * 1.0 = Constantly roaming
     */
    @Builder.Default
    private final float wanderlust = 0.5f;

    /**
     * The creature's intelligence level (affects pathfinding complexity and awareness).
     * 0.0 = Simple AI
     * 0.5 = Average intelligence
     * 1.0 = Highly intelligent
     */
    @Builder.Default
    private final float intelligence = 0.5f;

    /**
     * How loyal the creature is to its allies/friends.
     * 0.0 = No loyalty
     * 0.5 = Moderately loyal
     * 1.0 = Extremely loyal (helps allies in combat)
     */
    @Builder.Default
    private final float loyalty = 0.0f;

    /**
     * Detection range for targeting enemies (in blocks).
     */
    @Builder.Default
    private final double detectionRange = 32.0;

    /**
     * Attack range for melee attacks (in blocks).
     */
    @Builder.Default
    private final double attackRange = 2.0;

    /**
     * Movement speed multiplier when in combat.
     */
    @Builder.Default
    private final double combatSpeedMultiplier = 1.6;

    /**
     * Cooldown between attacks (in server ticks).
     */
    @Builder.Default
    private final int attackCooldown = 20;

    /**
     * Health percentage below which the creature might flee (if shy).
     * 0.3 = 30% health
     */
    @Builder.Default
    private final float fleeHealthThreshold = 0.3f;

    /**
     * Whether the creature can attack players in creative/spectator mode.
     */
    @Builder.Default
    private final boolean targetCreativePlayers = false;

    /**
     * Whether the creature prefers to attack from a distance if possible.
     */
    @Builder.Default
    private final boolean preferRangedCombat = false;

    /**
     * Creates a passive AI profile (won't attack unless provoked).
     */
    public static AIProfile passive() {
        return AIProfile.builder()
                .aggressiveness(0.0f)
                .shyness(0.3f)
                .wanderlust(0.6f)
                .build();
    }

    /**
     * Creates a neutral AI profile (attacks when nearby).
     */
    public static AIProfile neutral() {
        return AIProfile.builder()
                .aggressiveness(0.5f)
                .shyness(0.1f)
                .wanderlust(0.4f)
                .build();
    }

    /**
     * Creates an aggressive AI profile (actively hunts players).
     */
    public static AIProfile aggressive() {
        return AIProfile.builder()
                .aggressiveness(1.0f)
                .shyness(0.0f)
                .wanderlust(0.3f)
                .detectionRange(48.0)
                .build();
    }

    /**
     * Creates a boss AI profile (intelligent, aggressive, loyal to allies).
     */
    public static AIProfile boss() {
        return AIProfile.builder()
                .aggressiveness(0.9f)
                .shyness(0.0f)
                .intelligence(1.0f)
                .loyalty(1.0f)
                .territoriality(1.0f)
                .detectionRange(64.0)
                .attackRange(3.0)
                .combatSpeedMultiplier(2.0)
                .build();
    }

    /**
     * Creates a timid/shy AI profile (flees from threats).
     */
    public static AIProfile timid() {
        return AIProfile.builder()
                .aggressiveness(0.0f)
                .shyness(1.0f)
                .wanderlust(0.7f)
                .fleeHealthThreshold(0.8f)
                .build();
    }

    /**
     * Creates a curious AI profile (investigates players but doesn't attack).
     */
    public static AIProfile curious() {
        return AIProfile.builder()
                .aggressiveness(0.0f)
                .curiosity(1.0f)
                .wanderlust(0.5f)
                .intelligence(0.7f)
                .build();
    }
}

