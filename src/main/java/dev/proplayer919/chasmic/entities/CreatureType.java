package dev.proplayer919.chasmic.entities;

import net.minestom.server.entity.EntityType;

/**
 * @param attack         Flat attack damage
 * @param defense        Defense damage reduction (0-1, where 0.5 = 50% reduction)
 * @param criticalChance Critical chance (0-1, where 0.1 = 10% chance)
 */
public record CreatureType(String id, String name, EntityType entityType, int maxHealth, CreatureFamily creatureFamily,
                           float attack, float defense, float criticalChance, Class<? extends CustomCreature> creatureClass) {
}
