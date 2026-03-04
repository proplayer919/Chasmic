package dev.proplayer919.chasmic.entities;

import dev.proplayer919.chasmic.entities.creatures.Nella;
import dev.proplayer919.chasmic.entities.creatures.TestZombie;
import net.minestom.server.entity.EntityType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CreatureTypeRegistry {
    private final Map<String, CreatureType> creatureTypeRegistry = new HashMap<>();

    public CreatureTypeRegistry() {
        registerCreatureType(new CreatureType("test-zombie", "Test Zombie", EntityType.ZOMBIE, 50, CreatureFamily.UNDEAD, 5.0f, 0f, 0.5f, TestZombie.class));
        registerCreatureType(new CreatureType("nella", "Nella", EntityType.MAGMA_CUBE, 50000, CreatureFamily.VOLCANIC, 500.0f, 250.0f, 5.0f, Nella.class));
    }

    public void registerCreatureType(CreatureType creatureType) {
        creatureTypeRegistry.put(creatureType.id(), creatureType);
    }

    public CreatureType getCreatureType(String id) {
        return creatureTypeRegistry.get(id);
    }

    public Collection<CreatureType> getAllCreatureTypes() {
        return creatureTypeRegistry.values();
    }
}
