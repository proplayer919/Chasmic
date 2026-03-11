package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.helpers.ExpValue;
import dev.proplayer919.chasmic.service.Module;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Module that handles loading player data from MongoDB when players join
 */
public class PlayerDataModule implements Module {
    private static final Logger logger = LoggerFactory.getLogger(PlayerDataModule.class);
    private static final Tag<Boolean> NEEDS_RANK_LOAD = Tag.Boolean("needsRankLoad").defaultValue(false);
    private final MongoDBHandler mongoDBHandler;

    public PlayerDataModule(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();

            // Load or create player data asynchronously
            mongoDBHandler.loadOrCreatePlayerData(player.getUuid(), player.getUsername())
                    .thenAccept(playerData -> {
                        // Store the player data in the player object for later use
                        player.setPlayerData(playerData);
                        player.setExpValue(new ExpValue(playerData.getCurrentExp()));

                        player.dataLoadCallback();

                        // Mark that we need to load the rank on the first tick
                        player.setTag(NEEDS_RANK_LOAD, true);
                    })
                    .exceptionally(throwable -> {
                        // Log error and use default rank
                        logger.error("Failed to load player data for {}", player.getUsername(), throwable);
                        return null;
                    });
        });

        eventNode.addListener(PlayerTickEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            // Apply rank and permissions on the first tick after player data is loaded
            if (player.getTag(NEEDS_RANK_LOAD)) {
                player.removeTag(NEEDS_RANK_LOAD);

                if (player.getPlayerData() != null) {
                    // Set the player's rank from database
                    player.setRank(player.getPlayerData().getRank());

                    // Reload permissions to include custom permissions
                    player.reloadPermissions();
                }
            }
        });

        eventNode.addListener(PlayerDisconnectEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            if (player.getPlayerData() == null) {
                return;
            }

            player.saveActiveProfileInventory();
            mongoDBHandler.savePlayerData(player.getPlayerData());
        });
    }

    @Override
    public String getName() {
        return "PlayerDataModule";
    }
}
