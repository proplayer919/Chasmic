package dev.proplayer919.chasmic.module;


import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.helpers.ItemCreator;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MenuItemModule implements Module {
    private static final ItemStack menuItem = ItemCreator.createItem(1, Material.NETHER_STAR, "menu", "Chasmic Menu", "Use this to access the Chasmic Menu and manage your profile.", Rarity.SPECIAL, Collections.emptyList(), null, null, null, false);
    private static final ItemStack blockedSlotItem = ItemCreator.createItem(1, Material.BARRIER, "blocked", "Blocked Slot", "You cannot use this inventory slot in Chasmic!", Rarity.SPECIAL, Collections.emptyList(), null, null, null, false);
    private static final Collection<Integer> blockedSlots = List.of(36, 37, 38, 39, 40);
    private static final int menuSlot = 8;

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        // Register a listener to check when a player spawns
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            // Add the menu item to the crafting output slot of the player's inventory
            event.getPlayer().getInventory().setItemStack(menuSlot, menuItem);

            for (int blockedSlot : blockedSlots) {
                event.getPlayer().getInventory().setItemStack(blockedSlot, blockedSlotItem);
            }
        });

        // Register another listener to make sure the item isn't moved or replaced (cancel all move events that change the 9th slot)
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            if (event.getInventory() instanceof PlayerInventory playerInventory && (blockedSlots.contains(event.getSlot()) || event.getSlot() == menuSlot)) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public String getName() {
        return "MenuItemModule";
    }
}
