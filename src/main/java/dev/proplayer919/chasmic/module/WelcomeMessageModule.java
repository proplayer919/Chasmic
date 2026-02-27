package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Module that sends a welcome message to new players.
 * A player is considered "new" if they don't exist in the database.
 */
public class WelcomeMessageModule implements Module {
    private final String welcomeMessage;

    public WelcomeMessageModule(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public WelcomeMessageModule() {
        this("Welcome to the server!");
    }

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            if (event.isFirstSpawn()) {
                CustomPlayer player = (CustomPlayer) event.getPlayer();

                // Check if player data is loaded and if they're new
                if (player.getPlayerData() != null && player.getPlayerData().isNew()) {
                    event.getPlayer().sendMessage(welcomeMessage);
                }
            }
        });
    }

    @Override
    public String getName() {
        return "WelcomeMessageModule";
    }
}

