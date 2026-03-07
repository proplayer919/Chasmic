package dev.proplayer919.chasmic.gui;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.trait.InventoryEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;

import java.util.Map;

public record GuiScreen(Component title, InventoryType inventoryType, Map<Integer, GuiItem> items) {

    public Inventory getInventory() {
        Inventory inventory = new Inventory(inventoryType, title);
        for (Map.Entry<Integer, GuiItem> entry : items.entrySet()) {
            inventory.setItemStack(entry.getKey(), entry.getValue().getItemStack());
        }

        // Fill the empty slots
        for (int i = 0; i < inventoryType.getSize(); i++) {
            if (!items.containsKey(i)) {
                inventory.setItemStack(i, GuiConstants.FILLER_MATERIAL);
            }
        }

        EventNode<InventoryEvent> eventNode = inventory.eventNode();

        eventNode.addListener(InventoryPreClickEvent.class, event -> {
            int slot = event.getSlot();

            event.setCancelled(true);

            if (items.containsKey(slot) && (event.getClick() instanceof Click.Left(_))) {
                GuiItem guiItem = items.get(slot);
                GuiClickAction clickAction = guiItem.clickAction();
                if (clickAction != null) {
                    switch (clickAction.getActionType()) {
                        case GuiClickActionType.OPEN_SCREEN -> {
                            if (clickAction.getScreenToOpen() != null) {
                                event.getPlayer().openInventory(clickAction.getScreenToOpen().getInventory());
                            }
                        }
                        case GuiClickActionType.CLOSE_SCREEN -> event.getPlayer().closeInventory();
                        case GuiClickActionType.EXECUTE_COMMAND -> {
                            if (clickAction.getCommandToExecute() != null) {
                                CommandManager commandManager = MinecraftServer.getCommandManager();
                                commandManager.execute(event.getPlayer(), clickAction.getCommandToExecute());
                            }
                        }
                        case GuiClickActionType.OPEN_ACCESSORY_BAG -> {
                            if (event.getPlayer() instanceof CustomPlayer customPlayer) {
                                AccessoryBagScreen accessoryBagScreen = new AccessoryBagScreen(customPlayer);
                                event.getPlayer().openInventory(accessoryBagScreen.getInventory());
                            }
                        }
                    }
                }
            }
        });

        return inventory;
    }
}
