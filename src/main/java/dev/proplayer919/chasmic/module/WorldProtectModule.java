package dev.proplayer919.chasmic.module;


import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.items.ItemType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import org.jetbrains.annotations.NotNull;

public class WorldProtectModule implements Module {
    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        eventNode.addListener(PlayerBlockBreakEvent.class, event -> {
            if (!(event.getPlayer() instanceof CustomPlayer player)) {
                return;
            }

            // If the player doesn't have break permissions, cancel the event
            if (!player.hasPermission("admin.break")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You don't have permission to break blocks here!", NamedTextColor.RED));
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });

        eventNode.addListener(PlayerBlockPlaceEvent.class, event -> {
            if (!(event.getPlayer() instanceof CustomPlayer player)) {
                return;
            }

            // If the player doesn't have build permissions, cancel the event
            if (!player.hasPermission("admin.build")) {
                event.setCancelled(true);
                player.sendMessage(Component.text("You don't have permission to build blocks here!", NamedTextColor.RED));
            }

            if (player.getGameMode() != GameMode.CREATIVE) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public String getName() {
        return "WorldProtectModule";
    }
}
