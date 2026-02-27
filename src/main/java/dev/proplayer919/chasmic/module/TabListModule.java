package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.CustomPlayer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles tab list updates when players spawn
 */
public class TabListModule implements Module {

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            // Update tab list on spawn to ensure display name and order are set
            player.updateTabList();
        });
    }

    @Override
    public String getName() {
        return "TabListModule";
    }
}

