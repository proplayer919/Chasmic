package dev.proplayer919.chasmic.entities.creatures;

import dev.proplayer919.chasmic.ai.AIBuilder;
import dev.proplayer919.chasmic.ai.AIProfile;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.entities.CustomCreature;

public class TestZombie extends CustomCreature {
    public TestZombie() {
        super(Main.getCreatureTypeRegistry().getCreatureType("test-zombie"));

        // Create an aggressive AI profile for zombies
        AIProfile zombieAI = AIProfile.builder()
                .aggressiveness(0.8f)      // Very aggressive
                .shyness(0.0f)             // Not shy at all
                .wanderlust(0.4f)          // Moderate wandering
                .intelligence(0.3f)        // Basic intelligence
                .detectionRange(32.0)      // Can detect players from 32 blocks
                .attackRange(2.0)          // Melee range
                .combatSpeedMultiplier(1.6)
                .attackCooldown(20)        // 1 second between attacks
                .build();

        setAiProfile(zombieAI);

        // Use the new AI builder to set up the zombie's behavior
        AIBuilder.setupStandardAI(this, zombieAI);
    }
}