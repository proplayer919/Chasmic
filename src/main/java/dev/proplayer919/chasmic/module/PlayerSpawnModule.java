package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.entities.creatures.TestZombie;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles player spawning
 */
public class PlayerSpawnModule implements Module {

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> event.setSpawningInstance(Main.getSpawnInstance()));

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            event.getPlayer().teleport(Main.getSpawnPos());
            event.getPlayer().setRespawnPoint(Main.getSpawnPos());

            if (event.getPlayer() instanceof CustomPlayer customPlayer) {
                customPlayer.showSidebar();
            }

            TestZombie zombie = new TestZombie();
            zombie.setInstance(Main.getSpawnInstance(), Main.getSpawnPos());

            Main.getNpc().addPlayerViewer(event.getPlayer());
        });
    }

    @Override
    public String getName() {
        return "PlayerSpawnModule";
    }
}
