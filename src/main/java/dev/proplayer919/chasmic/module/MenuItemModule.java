package dev.proplayer919.chasmic.module;


import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.helpers.ItemCreator;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class MenuItemModule implements Module {
    private static final ItemStack menuItem = ItemCreator.createItem(1, Material.NETHER_STAR, "menu", "Chasmic Menu", "Use this to access the Chasmic Menu and manage your profile.", Rarity.SPECIAL, Collections.emptyList(), null, null, null);
    private static final int itemSlot = 36;

    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        // Register a listener to check when a player spawns
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            // Add the menu item to the 9th slot of the player's hotbar
            event.getPlayer().getInventory().setItemStack(itemSlot, menuItem);
        });

        // Register another listener to make sure the item isn't moved or replaced (cancel all move events that change the 9th slot)
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            if (event.getInventory() instanceof PlayerInventory playerInventory && event.getSlot() == itemSlot) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public String getName() {
        return "MenuItemModule";
    }
}
