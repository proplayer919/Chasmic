package dev.proplayer919.chasmic.entities.creatures;

import dev.proplayer919.chasmic.entities.ai.AIBuilder;
import dev.proplayer919.chasmic.entities.ai.AIProfile;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.entities.CustomCreature;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeModifier;
import net.minestom.server.entity.attribute.AttributeOperation;

import java.util.UUID;

public class Nella extends CustomCreature {
    public Nella() {
        super(Main.getServiceContainer().getCreatureTypeRegistry().getCreatureType("nella"));

        getAttribute(Attribute.SCALE).addModifier(new AttributeModifier(UUID.randomUUID().toString(), 5.0, AttributeOperation.ADD_VALUE));

        // Create a boss AI profile for Nella
        AIProfile nellaAI = AIProfile.builder()
                .aggressiveness(0.95f)      // Extremely aggressive
                .shyness(0.0f)              // Fearless
                .intelligence(1.0f)         // Highly intelligent
                .loyalty(1.0f)              // Loyal to volcanic allies
                .territoriality(1.0f)       // Highly territorial
                .wanderlust(0.3f)           // Doesn't wander much
                .detectionRange(64.0)       // Can detect players from very far
                .attackRange(3.0)           // Larger attack range due to size
                .combatSpeedMultiplier(2.0) // Fast in combat
                .attackCooldown(15)         // Attacks faster than normal
                .build();

        setAiProfile(nellaAI);

        // Use the new AI builder to set up boss behavior
        AIBuilder.setupStandardAI(this, nellaAI);
    }
}