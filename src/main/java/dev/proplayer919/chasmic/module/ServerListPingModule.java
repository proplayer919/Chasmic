package dev.proplayer919.chasmic.module;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.Status;
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
        this(createDefaultMotd(), 500);
    }

    private static Component createDefaultMotd() {
        Component line1 = Component.empty()
                .append(Component.text("                     "))
                .append(Component.text("Chasmic", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" <1.21.11>", NamedTextColor.RED));

        Component line2 = Component.empty()
                .append(Component.text("               Custom Items ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("| ", NamedTextColor.GOLD))
                .append(Component.text("Story MMORPG", NamedTextColor.AQUA));

        return Component.empty()
                .append(line1)
                .append(Component.newline())
                .append(line2);
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
                            .build())
                    .build());
        });
    }

    @Override
    public String getName() {
        return "ServerListPingModule";
    }
}

