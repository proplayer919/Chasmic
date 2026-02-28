package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles loading player data from MongoDB when players join
 */
public class PlayerDataModule implements Module {
    private static final Tag<Boolean> NEEDS_RANK_LOAD = Tag.Boolean("needsRankLoad").defaultValue(false);
    private final MongoDBHandler mongoDBHandler;

    public PlayerDataModule(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();

            // Load or create player data asynchronously
            mongoDBHandler.loadOrCreatePlayerData(player.getUuid(), player.getUsername())
                    .thenAccept(playerData -> {
                        // Store the player data in the player object for later use
                        player.setPlayerData(playerData);

                        // Mark that we need to load the rank on the first tick
                        player.setTag(NEEDS_RANK_LOAD, true);
                    })
                    .exceptionally(throwable -> {
                        // Log error and use default rank
                        System.err.println("Failed to load player data for " + player.getUsername() + ": " + throwable.getMessage());
                        throwable.printStackTrace();
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
    }

    @Override
    public String getName() {
        return "PlayerDataModule";
    }
}

