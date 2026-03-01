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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Module that handles server list ping (MOTD and player info)
 */
public class ServerListPingModule implements Module {
    private final Component description;
    private final int maxPlayers;
    private final byte[] favicon;

    public ServerListPingModule(Component description, int maxPlayers) {
        this.description = description;
        this.maxPlayers = maxPlayers;
        this.favicon = loadFavicon();
    }

    public ServerListPingModule() {
        this(createDefaultMotd(), 500);
    }

    private static Component createDefaultMotd() {
        Component line1 = Component.empty()
                .append(Component.text("                     "))
                .append(Component.text("Chasmic", NamedTextColor.GREEN, TextDecoration.BOLD))
                .append(Component.text(" [1.21.11]", NamedTextColor.RED));

        Component line2 = Component.empty()
                .append(Component.text("               Custom Items ", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("| ", NamedTextColor.GOLD))
                .append(Component.text("Story MMORPG", NamedTextColor.AQUA));

        return Component.empty()
                .append(line1)
                .append(Component.newline())
                .append(line2);
    }

    private static byte[] loadFavicon() {
        try (InputStream inputStream = ServerListPingModule.class.getResourceAsStream("/logo.png")) {
            if (inputStream == null) {
                System.err.println("Failed to load logo.png from resources");
                return null;
            }

            BufferedImage image = ImageIO.read(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            System.err.println("Error loading favicon: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(ServerListPingEvent.class, event -> {
            int onlinePlayers = MinecraftServer.getConnectionManager().getOnlinePlayerCount();

            Status.Builder statusBuilder = Status.builder()
                    .description(description)
                    .playerInfo(Status.PlayerInfo.builder()
                            .onlinePlayers(onlinePlayers)
                            .maxPlayers(maxPlayers)
                            .build());

            if (favicon != null) {
                statusBuilder.favicon(favicon);
            }

            event.setStatus(statusBuilder.build());
        });
    }

    @Override
    public String getName() {
        return "ServerListPingModule";
    }
}

