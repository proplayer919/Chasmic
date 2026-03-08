package dev.proplayer919.chasmic;

import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.entities.CreatureTypeRegistry;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import dev.proplayer919.chasmic.location.LocationRegistry;
import dev.proplayer919.chasmic.punishment.PunishmentManager;
import lombok.Getter;

/**
 * Service container for dependency injection
 * Replaces static getters from Main class with proper DI
 */
@Getter
public class ServiceContainer {
    private final MongoDBHandler mongoDBHandler;
    private final CustomItemRegistry customItemRegistry;
    private final AccessoryRegistry accessoryRegistry;
    private final CreatureTypeRegistry creatureTypeRegistry;
    private final LocationRegistry locationRegistry;
    private final PunishmentManager punishmentManager;

    public ServiceContainer(
            MongoDBHandler mongoDBHandler,
            PunishmentManager punishmentManager
    ) {
        this.mongoDBHandler = mongoDBHandler;
        this.punishmentManager = punishmentManager;

        // Initialize registries that don't depend on Main statics
        // ItemActionRegistry must be created first in Main since CustomItemRegistry depends on it
        this.customItemRegistry = new CustomItemRegistry();
        this.accessoryRegistry = new AccessoryRegistry();
        this.creatureTypeRegistry = new CreatureTypeRegistry();
        this.locationRegistry = new LocationRegistry();
    }
}