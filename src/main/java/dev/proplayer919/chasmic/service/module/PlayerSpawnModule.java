package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.entities.creatures.TestZombie;
import dev.proplayer919.chasmic.service.Module;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import org.jspecify.annotations.NonNull;

/**
 * Module that handles player spawning
 */
public class PlayerSpawnModule implements Module {

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> event.setSpawningInstance(Main.getSpawnInstance()));

        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();

            player.teleport(Main.getSpawnPos());
            player.setRespawnPoint(Main.getSpawnPos());
            player.setCurrentLocation(Main.getServiceContainer().getLocationRegistry().getLocation("chasmic_city"));

            player.showSidebar();

            TestZombie zombie = new TestZombie();
            zombie.setInstance(Main.getSpawnInstance(), Main.getSpawnPos());

            Main.getNpc().addPlayerViewer(player);
        });
    }

    @Override
    public String getName() {
        return "PlayerSpawnModule";
    }
}
