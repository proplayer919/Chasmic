package dev.proplayer919.chasmic.module;


import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.PlayerHeads;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.gui.GuiClickAction;
import dev.proplayer919.chasmic.gui.GuiClickActionType;
import dev.proplayer919.chasmic.gui.GuiItem;
import dev.proplayer919.chasmic.gui.GuiScreen;
import dev.proplayer919.chasmic.helpers.ItemCreator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerGameModeChangeEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class MenuItemModule implements Module {
    private static final ItemStack MENU_ITEM = ItemCreator.createItem(1, Material.NETHER_STAR, "menu", "Chasmic Menu", "Use this to access the Chasmic Menu and manage your profile.", Rarity.SPECIAL, Collections.emptyList(), null, null, null, false);
    private static final ItemStack BLOCKED_SLOT_ITEM = ItemCreator.createItem(1, Material.BARRIER, "blocked", "Blocked Slot", "You cannot use this inventory slot in Chasmic!", Rarity.SPECIAL, Collections.emptyList(), null, null, null, false);
    private static final Collection<Integer> BLOCKED_SLOTS = List.of(36, 37, 38, 39, 40);
    private static final int MENU_SLOT = 8;

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        // Add menu items when player spawns
        eventNode.addListener(PlayerSpawnEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.getGameMode() == GameMode.SURVIVAL) {
                giveMenuItems(player);
            }
        });

        // Register a listener to check when a player switches to survival mode and add the menu item if they don't have it
        eventNode.addListener(PlayerGameModeChangeEvent.class, event -> {
            if (event.getNewGameMode() != GameMode.SURVIVAL) {
                return;
            }

            giveMenuItems(event.getPlayer());
        });

        // Register another listener to make sure the item isn't moved or replaced (cancel all move events that change the 9th slot)
        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
                return;
            }

            if (event.getInventory() instanceof PlayerInventory && (BLOCKED_SLOTS.contains(event.getSlot()) || event.getSlot() == MENU_SLOT)) {
                event.setCancelled(true);
            }
        });

        // Register a listener for when the menu item is clicked to open the menu screen
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (event.getItemStack().equals(MENU_ITEM) && event.getPlayer() instanceof CustomPlayer player) {
                GuiScreen menuScreen = createMenuScreen(player);
                player.openInventory(menuScreen.getInventory());
                event.setCancelled(true);
            }
        });
    }

    @Override
    public String getName() {
        return "MenuItemModule";
    }

    private void giveMenuItems(Player player) {
        player.getInventory().setItemStack(MENU_SLOT, MENU_ITEM);

        for (int blockedSlot : BLOCKED_SLOTS) {
            player.getInventory().setItemStack(blockedSlot, BLOCKED_SLOT_ITEM);
        }
    }

    /**
     * Creates a fresh menu screen for the given player
     */
    private GuiScreen createMenuScreen(CustomPlayer player) {
        Map<Integer, GuiItem> menuItems = new HashMap<>();

        menuItems.put(10, new GuiItem(Material.PLAYER_HEAD, Component.text("Profile"), "View and manage your Chasmic profile, including stats, achievements, and cosmetics.", new GuiClickAction(GuiClickActionType.OPEN_SCREEN, createProfileScreen(player))));

        List<Component> expDescription = new ArrayList<>();
        expDescription.add(Component.text("Current Level: ").color(NamedTextColor.GRAY).append(Component.text(player.getExpValue().getLevel()).color(NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false));
        expDescription.add(Component.text("Current EXP: ").color(NamedTextColor.GRAY).append(Component.text(player.getExpValue().getExpToPreviousLevel()).color(NamedTextColor.AQUA).append(Component.text("/").color(NamedTextColor.DARK_GRAY).append(Component.text(player.getExpValue().getExpToNextLevel()).color(NamedTextColor.GRAY)))).decoration(TextDecoration.ITALIC, false));

        menuItems.put(11, new GuiItem(Material.EXPERIENCE_BOTTLE, Component.text("Chasmic Level").color(NamedTextColor.GREEN), expDescription, null));

        // Add Accessory Bag button
        List<Component> accessoryBagDescription = new ArrayList<>();
        accessoryBagDescription.add(Component.text("Store and manage your accessories").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        accessoryBagDescription.add(Component.empty());
        accessoryBagDescription.add(Component.text("Slots: ").color(NamedTextColor.GRAY).append(Component.text(player.getPlayerData().getAccessorySlots()).color(NamedTextColor.GREEN)).decoration(TextDecoration.ITALIC, false));

        menuItems.put(12, ItemCreator.createGuiItemWithTexture(
                PlayerHeads.ACCESSORY_BAG,
                Component.text("Accessory Bag").color(NamedTextColor.LIGHT_PURPLE),
                accessoryBagDescription,
                new GuiClickAction(GuiClickActionType.OPEN_ACCESSORY_BAG)
        ));

        return new GuiScreen(
                Component.text("Chasmic Menu"),
                InventoryType.CHEST_4_ROW,
                menuItems
        );
    }

    private GuiScreen createProfileScreen(CustomPlayer player) {
        Map<Integer, GuiItem> profileItems = new HashMap<>();

        return new GuiScreen(
                Component.text("Profile"),
                InventoryType.CHEST_3_ROW,
                profileItems
        );
    }
}
