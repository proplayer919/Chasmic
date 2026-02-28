package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

/**
 * Custom argument type for player names that excludes NPCs from autocomplete.
 * Only real CustomPlayer instances will be suggested.
 */
public class PlayerNameArgument extends ArgumentWord {

    public PlayerNameArgument(String id) {
        super(id);

        // Set up dynamic suggestions that only include real players (not NPCs)
        setSuggestionCallback((sender, context, suggestion) -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                // Only suggest real players (CustomPlayer instances)
                // NPCs are LivingEntity instances, not Player instances managed by ConnectionManager
                if (player instanceof CustomPlayer) {
                    suggestion.addEntry(new SuggestionEntry(player.getUsername()));
                }
            }
        });
    }

    /**
     * Creates a new PlayerNameArgument with the given identifier
     */
    public static PlayerNameArgument playerName(String id) {
        return new PlayerNameArgument(id);
    }
}


