package dev.proplayer919.chasmic.module;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;
import net.minestom.server.utils.identity.NamedAndIdentified;
import org.jetbrains.annotations.NotNull;

/**
 * Module that handles server list ping (MOTD and player info)
 */
public class ServerListPingModule implements Module {
    private final Component description;
    private final int maxPlayers;

    public ServerListPingModule(Component description, int maxPlayers) {
        this.description = description;
        this.maxPlayers = maxPlayers;
    }

    public ServerListPingModule() {
        this(Component.text("Welcome to my Minecraft server!", NamedTextColor.GOLD), 500);
    }

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(ServerListPingEvent.class, event -> {
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayerCount();

            event.setStatus(Status.builder()
                    .description(description)
                    .playerInfo(Status.PlayerInfo.builder()
                            .onlinePlayers(onlinePlayers)
                            .maxPlayers(maxPlayers)
                            .sample(NamedAndIdentified.named("Notch"))
                            .sample(NamedAndIdentified.named(Component.text("Herobrine", NamedTextColor.AQUA)))
                            .build())
                    .build());
        });
    }

    @Override
    public String getName() {
        return "ServerListPingModule";
    }
}

