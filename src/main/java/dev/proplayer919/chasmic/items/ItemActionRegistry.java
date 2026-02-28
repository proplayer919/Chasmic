package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.items.actions.ItemWarpAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;

import java.util.HashMap;
import java.util.Map;

public class ItemActionRegistry {
    private final Map<String, ItemAction> itemActions = new HashMap<>();

    public ItemActionRegistry() {
        // Register items actions here
        registerItemAction(new ItemAction("warp", 0.5, 20, new ItemWarpAction()));
    }

    public void registerEvents(EventNode<Event> eventNode) {
        // Register a right click listener to handle item actions
        eventNode.addListener(net.minestom.server.event.player.PlayerUseItemEvent.class, event -> {
            if (!(event.getPlayer() instanceof CustomPlayer player)) {
                return;
            }

            // Check if the item has an action
            String actionId = event.getItemStack().getTag(CustomItem.itemActionTag);

            if (actionId == null) {
                return;
            }

            ItemAction itemAction = getItemAction(actionId);

            if (itemAction == null) {
                return;
            }

            // Check cooldown
            if (player.getItemCooldowns().getOrDefault(actionId, new java.util.Date(0)).after(new java.util.Date())) {
                player.sendMessage(Component.text("This item is on cooldown!").color(NamedTextColor.RED));
            }

            // Check mana cost
            int playerMana = player.getCustomMana();
            if (playerMana < itemAction.manaCost()) {
                player.sendMessage(Component.text("You don't have enough mana to use this item!").color(NamedTextColor.RED));
                return;
            }

            // Deduct mana cost
            player.setCustomMana(playerMana - itemAction.manaCost());

            // Execute the action
            itemAction.actionHandler().handleAction(player);

            // Set cooldown
            player.usedItem(actionId);
        });
    }

    public void registerItemAction(ItemAction itemAction) {
        itemActions.put(itemAction.id(), itemAction);
    }

    public ItemAction getItemAction(String id) {
        return itemActions.get(id);
    }
}
