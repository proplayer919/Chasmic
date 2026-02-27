package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.PlayerRank;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;

/**
 * Utility module for managing player ranks
 * This provides helper methods for updating ranks in both memory and database
 */
public class RankManagementModule implements Module {
    private final MongoDBHandler mongoDBHandler;

    public RankManagementModule(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        // This module doesn't attach any event listeners
        // It provides utility methods for rank management
    }

    /**
     * Updates a player's rank in both memory and database
     * @param player The player to update
     * @param rank The new rank
     */
    public void updatePlayerRank(CustomPlayer player, PlayerRank rank) {
        // Update in-memory rank (also updates tab list)
        player.setRank(rank);

        // Update player data
        if (player.getPlayerData() != null) {
            player.getPlayerData().setRank(rank);
        }

        // Update in database
        mongoDBHandler.updatePlayerRank(player.getUuid(), rank)
                .exceptionally(throwable -> {
                    System.err.println("Failed to update rank in database for " + player.getUsername());
                    throwable.printStackTrace();
                    return null;
                });
    }

    /**
     * Updates a player's rank by UUID (for offline players)
     * @param uuid The player's UUID
     * @param rank The new rank
     */
    public void updatePlayerRank(java.util.UUID uuid, PlayerRank rank) {
        mongoDBHandler.updatePlayerRank(uuid, rank)
                .exceptionally(throwable -> {
                    System.err.println("Failed to update rank in database for UUID: " + uuid);
                    throwable.printStackTrace();
                    return null;
                });
    }

    @Override
    public String getName() {
        return "RankManagementModule";
    }
}

