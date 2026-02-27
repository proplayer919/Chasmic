package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles loading player data from MongoDB when players join
 */
public class PlayerDataModule implements Module {
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
                        // Set the player's rank from database
                        player.setRank(playerData.getRank());

                        // Store the player data in the player object for later use
                        player.setPlayerData(playerData);

                        // Reload permissions to include custom permissions
                        player.reloadPermissions();
                    })
                    .exceptionally(throwable -> {
                        // Log error and use default rank
                        System.err.println("Failed to load player data for " + player.getUsername() + ": " + throwable.getMessage());
                        throwable.printStackTrace();
                        return null;
                    });
        });
    }

    @Override
    public String getName() {
        return "PlayerDataModule";
    }
}

