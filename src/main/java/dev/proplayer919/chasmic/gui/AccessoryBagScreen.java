package dev.proplayer919.chasmic.gui;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.accessories.Accessory;
import dev.proplayer919.chasmic.accessories.AccessoryBag;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.items.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the Accessory Bag GUI screen where players can store and manage their accessories
 */
public class AccessoryBagScreen {
    private static final ItemStack LOCKED_SLOT_ITEM = ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
            .set(DataComponents.ITEM_NAME,
                Component.text("Locked Slot").color(NamedTextColor.RED))
            .lore(List.of(Component.text("Unlock more accessory slots").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)))
            .build();

    private final CustomPlayer player;
    private final Component title;
    private final InventoryType inventoryType;

    public AccessoryBagScreen(CustomPlayer player) {
        this.player = player;
        this.title = Component.text("Accessory Bag");
        this.inventoryType = calculateInventoryType(player.getPlayerData().getAccessorySlots());
    }

    /**
     * Calculates the appropriate inventory type based on the number of accessory slots
     */
    private InventoryType calculateInventoryType(int slots) {
        if (slots <= 0) {
            return InventoryType.CHEST_1_ROW;
        } else if (slots <= 9) {
            return InventoryType.CHEST_1_ROW;
        } else if (slots <= 18) {
            return InventoryType.CHEST_2_ROW;
        } else if (slots <= 27) {
            return InventoryType.CHEST_3_ROW;
        } else if (slots <= 36) {
            return InventoryType.CHEST_4_ROW;
        } else if (slots <= 45) {
            return InventoryType.CHEST_5_ROW;
        } else {
            return InventoryType.CHEST_6_ROW;
        }
    }

    /**
     * Creates and returns the inventory for the accessory bag
     */
    public Inventory getInventory() {
        Inventory inventory = new Inventory(inventoryType, title);
        PlayerData playerData = player.getPlayerData();
        AccessoryBag accessoryBag = playerData.getAccessoryBagObject();

        int accessorySlots = playerData.getAccessorySlots();
        int inventorySize = inventoryType.getSize();

        // Convert accessory bag to a list for indexed access
        List<Accessory> accessories = new ArrayList<>(accessoryBag.accessories());

        // Fill the inventory
        for (int i = 0; i < inventorySize; i++) {
            if (i < accessorySlots) {
                // This is an unlocked slot
                if (i < accessories.size()) {
                    // There's an accessory in this slot
                    inventory.setItemStack(i, accessories.get(i).getItemStack());
                }
                // Otherwise, leave it empty (null/air)
            } else {
                // This is a locked slot
                inventory.setItemStack(i, LOCKED_SLOT_ITEM);
            }
        }

        // Add event handling
        EventNode<InventoryEvent> eventNode = inventory.eventNode();

        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            int slot = event.getSlot();

            // Don't allow interaction with locked slots
            if (slot >= accessorySlots) {
                event.setCancelled(true);
                return;
            }

            // Get the clicked item and cursor item
            ItemStack clickedItem = event.getClickedItem();
            ItemStack cursorItem = event.getPlayer().getInventory().getCursorItem();

            // Allow players to take items out
            if (!clickedItem.isAir()) {
                String itemId = CustomItem.getItemId(clickedItem);
                if (itemId != null) {
                    Accessory accessory = Main.getAccessoryRegistry().getAccessoryById(itemId);
                    if (accessory != null) {
                        // This is a valid accessory, allow the action
                        return;
                    }
                }
            }

            // Allow players to put accessories in
            if (!cursorItem.isAir()) {
                String itemId = CustomItem.getItemId(cursorItem);
                if (itemId != null) {
                    Accessory accessory = Main.getAccessoryRegistry().getAccessoryById(itemId);
                    if (accessory != null) {
                        // This is a valid accessory, allow the action
                        return;
                    }
                }
                // Not an accessory, cancel the event
                event.setCancelled(true);
            }
        });

        // Save the accessory bag when the inventory is closed
        eventNode.addListener(InventoryCloseEvent.class, _ -> saveAccessoryBag(inventory));

        return inventory;
    }

    /**
     * Saves the current state of the accessory bag to the player data
     */
    private void saveAccessoryBag(Inventory inventory) {
        PlayerData playerData = player.getPlayerData();
        int accessorySlots = playerData.getAccessorySlots();

        List<String> accessoryIds = new ArrayList<>();

        // Iterate through unlocked slots and save accessories
        for (int i = 0; i < accessorySlots; i++) {
            ItemStack item = inventory.getItemStack(i);
            if (!item.isAir()) {
                String itemId = CustomItem.getItemId(item);
                if (itemId != null) {
                    Accessory accessory = Main.getAccessoryRegistry().getAccessoryById(itemId);
                    if (accessory != null) {
                        accessoryIds.add(itemId);
                    }
                }
            }
        }

        // Update the player data
        playerData.setAccessoryBag(accessoryIds);

        // Save to database
        Main.getMongoDBHandler().savePlayerData(playerData);
    }
}

