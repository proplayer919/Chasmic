package dev.proplayer919.chasmic.module;

import dev.proplayer919.chasmic.player.CustomPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.PlayerInventory;
import org.jspecify.annotations.NonNull;

/**
 * Module that monitors equipment changes and invalidates stat caches when gear changes
 */
public class PlayerEquipmentModule implements Module {
    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        // Listen for inventory clicks (main hand item changes and armor changes)
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            Player player = event.getPlayer();

            // Only process player inventory clicks
            if (!(event.getInventory() instanceof PlayerInventory) || !(player instanceof CustomPlayer customPlayer)) {
                return;
            }

            // Schedule a check after the click completes to mark stats dirty
            // We do this in the next tick to allow the inventory change to process
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                // Mark stats dirty to recalculate
                customPlayer.getStatsManager().markAllStatsDirty();
                customPlayer.markSpeedStatDirty();
                customPlayer.getUiManager().markActionBarDirty();
            }).schedule();
        });
    }

    @Override
    public String getName() {
        return "PlayerEquipmentModule";
    }
}