package dev.proplayer919.chasmic.entities.ai;

import lombok.Getter;

/**
 * Defines the AI personality and behavior traits for a creature.
 * All trait values must be in the inclusive range [0.0, 1.0].
 */
@Getter
public class AIProfile {
    public static final float MIN_TRAIT_VALUE = 0.0f;
    public static final float MAX_TRAIT_VALUE = 1.0f;
    public static final float MIN_ACTIVE_TRAIT = 0.0001f;

    public static final float FLEE_HEALTH_RECOVERY_BUFFER = 0.2f;
    public static final double TARGET_STICKINESS_RANGE_MULTIPLIER = 1.15d;

    private final float aggressiveness;
    private final float shyness;
    private final float curiosity;
    private final float sociability;
    private final float territoriality;
    private final float wanderlust;
    private final float intelligence;
    private final float loyalty;
    private final double detectionRange;
    private final double attackRange;
    private final double combatSpeedMultiplier;
    private final int attackCooldown;
    private final float fleeHealthThreshold;
    private final boolean targetCreativePlayers;
    private final boolean preferRangedCombat;

    private AIProfile(AIProfileBuilder builder) {
        this.aggressiveness = validateTrait("aggressiveness", builder.aggressiveness);
        this.shyness = validateTrait("shyness", builder.shyness);
        this.curiosity = validateTrait("curiosity", builder.curiosity);
        this.sociability = validateTrait("sociability", builder.sociability);
        this.territoriality = validateTrait("territoriality", builder.territoriality);
        this.wanderlust = validateTrait("wanderlust", builder.wanderlust);
        this.intelligence = validateTrait("intelligence", builder.intelligence);
        this.loyalty = validateTrait("loyalty", builder.loyalty);
        this.fleeHealthThreshold = validateTrait("fleeHealthThreshold", builder.fleeHealthThreshold);

        if (builder.detectionRange <= 0) {
            throw new IllegalArgumentException("detectionRange must be > 0");
        }
        if (builder.attackRange <= 0) {
            throw new IllegalArgumentException("attackRange must be > 0");
        }
        if (builder.attackRange > builder.detectionRange) {
            throw new IllegalArgumentException("attackRange cannot be greater than detectionRange");
        }
        if (builder.combatSpeedMultiplier <= 0) {
            throw new IllegalArgumentException("combatSpeedMultiplier must be > 0");
        }
        if (builder.attackCooldown < 1) {
            throw new IllegalArgumentException("attackCooldown must be >= 1 tick");
        }

        this.detectionRange = builder.detectionRange;
        this.attackRange = builder.attackRange;
        this.combatSpeedMultiplier = builder.combatSpeedMultiplier;
        this.attackCooldown = builder.attackCooldown;
        this.targetCreativePlayers = builder.targetCreativePlayers;
        this.preferRangedCombat = builder.preferRangedCombat;
    }

    public static AIProfileBuilder builder() {
        return new AIProfileBuilder();
    }

    private static float validateTrait(String name, float value) {
        if (value < MIN_TRAIT_VALUE || value > MAX_TRAIT_VALUE) {
            throw new IllegalArgumentException(name + " must be within [0.0, 1.0], got " + value);
        }
        return value;
    }

    public static class AIProfileBuilder {
        private float aggressiveness = 0.5f;
        private float shyness = 0.0f;
        private float curiosity = 0.0f;
        private float sociability = 0.0f;
        private float territoriality = 0.0f;
        private float wanderlust = 0.5f;
        private float intelligence = 0.5f;
        private float loyalty = 0.0f;
        private double detectionRange = 32.0;
        private double attackRange = 2.0;
        private double combatSpeedMultiplier = 1.6;
        private int attackCooldown = 20;
        private float fleeHealthThreshold = 0.3f;
        private boolean targetCreativePlayers = false;
        private boolean preferRangedCombat = false;

        public AIProfileBuilder aggressiveness(float aggressiveness) {
            this.aggressiveness = aggressiveness;
            return this;
        }

        public AIProfileBuilder shyness(float shyness) {
            this.shyness = shyness;
            return this;
        }

        public AIProfileBuilder curiosity(float curiosity) {
            this.curiosity = curiosity;
            return this;
        }

        public AIProfileBuilder sociability(float sociability) {
            this.sociability = sociability;
            return this;
        }

        public AIProfileBuilder territoriality(float territoriality) {
            this.territoriality = territoriality;
            return this;
        }

        public AIProfileBuilder wanderlust(float wanderlust) {
            this.wanderlust = wanderlust;
            return this;
        }

        public AIProfileBuilder intelligence(float intelligence) {
            this.intelligence = intelligence;
            return this;
        }

        public AIProfileBuilder loyalty(float loyalty) {
            this.loyalty = loyalty;
            return this;
        }

        public AIProfileBuilder detectionRange(double detectionRange) {
            this.detectionRange = detectionRange;
            return this;
        }

        public AIProfileBuilder attackRange(double attackRange) {
            this.attackRange = attackRange;
            return this;
        }

        public AIProfileBuilder combatSpeedMultiplier(double combatSpeedMultiplier) {
            this.combatSpeedMultiplier = combatSpeedMultiplier;
            return this;
        }

        public AIProfileBuilder attackCooldown(int attackCooldown) {
            this.attackCooldown = attackCooldown;
            return this;
        }

        public AIProfileBuilder fleeHealthThreshold(float fleeHealthThreshold) {
            this.fleeHealthThreshold = fleeHealthThreshold;
            return this;
        }

        public AIProfileBuilder targetCreativePlayers(boolean targetCreativePlayers) {
            this.targetCreativePlayers = targetCreativePlayers;
            return this;
        }

        public AIProfileBuilder preferRangedCombat(boolean preferRangedCombat) {
            this.preferRangedCombat = preferRangedCombat;
            return this;
        }

        public AIProfile build() {
            return new AIProfile(this);
        }
    }

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
