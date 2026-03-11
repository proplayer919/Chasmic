package dev.proplayer919.chasmic.player.gui;

import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.player.CustomPlayer;
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

    public Inventory getInventory(AccessoryRegistry accessoryRegistry, MongoDBHandler mongoDBHandler) {
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

            if (!items.containsKey(slot)) {
                return;
            }

            GuiItem guiItem = items.get(slot);
            GuiClickAction clickAction = null;

            if (event.getClick() instanceof Click.Left(_)) {
                clickAction = guiItem.leftClickAction();
            } else if (event.getClick() instanceof Click.Right(_)) {
                clickAction = guiItem.rightClickAction();
            }

            if (clickAction != null) {
                switch (clickAction.getActionType()) {
                    case OPEN_SCREEN -> {
                        if (clickAction.getScreenToOpen() != null) {
                            event.getPlayer().openInventory(clickAction.getScreenToOpen().getInventory(accessoryRegistry, mongoDBHandler));
                        }
                    }
                    case CLOSE_SCREEN -> event.getPlayer().closeInventory();
                    case EXECUTE_COMMAND -> {
                        if (clickAction.getCommandToExecute() != null) {
                            CommandManager commandManager = MinecraftServer.getCommandManager();
                            commandManager.execute(event.getPlayer(), clickAction.getCommandToExecute());
                        }
                    }
                    case OPEN_ACCESSORY_BAG -> {
                        if (event.getPlayer() instanceof CustomPlayer customPlayer) {
                            AccessoryBagScreen accessoryBagScreen = new AccessoryBagScreen(customPlayer);
                            event.getPlayer().openInventory(accessoryBagScreen.getInventory(accessoryRegistry, mongoDBHandler));
                        }
                    }
                    case CUSTOM -> {
                        if (clickAction.getCustomAction() != null) {
                            clickAction.getCustomAction().accept(event);
                        }
                    }
                }
            }
        });

        return inventory;
    }
}
