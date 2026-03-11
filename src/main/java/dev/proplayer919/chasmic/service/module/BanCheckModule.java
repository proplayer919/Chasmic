package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.player.punishment.Punishment;
import dev.proplayer919.chasmic.player.punishment.PunishmentManager;
import dev.proplayer919.chasmic.player.punishment.PunishmentMessages;
import dev.proplayer919.chasmic.service.Module;
import net.kyori.adventure.text.Component;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.jspecify.annotations.NonNull;

/**
 * Module that checks if a player is banned before they join
 */
public class BanCheckModule implements Module {
    private final PunishmentManager punishmentManager;

    public BanCheckModule(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, this::onPlayerConfiguration);
    }

    @Override
    public String getName() {
        return "BanCheckModule";
    }

    private void onPlayerConfiguration(AsyncPlayerConfigurationEvent event) {
        // Check if player has an active ban
        Punishment activeBan = punishmentManager.getActiveBan(event.getPlayer().getUuid());

        if (activeBan != null && activeBan.isActive()) {
            // Player is banned, create and send ban message using utility
            Component banMessage = PunishmentMessages.createBanMessage(activeBan);
            event.getPlayer().kick(banMessage);
        }
    }
}

