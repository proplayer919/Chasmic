package dev.proplayer919.chasmic.service.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.service.Module;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import org.jspecify.annotations.NonNull;

/**
 * Module that handles chat formatting with rank-based display
 */
public class ChatModule implements Module {

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerChatEvent.class, event -> {
            CustomPlayer player = (CustomPlayer) event.getPlayer();
            Component chatMessage = player.buildMessage(event.getRawMessage());

            // Cancel the default chat event and send custom formatted message to all players
            event.setCancelled(true);
            MinecraftServer.getConnectionManager().getOnlinePlayers()
                    .forEach(p -> p.sendMessage(chatMessage));
        });
    }

    @Override
    public String getName() {
        return "ChatModule";
    }
}