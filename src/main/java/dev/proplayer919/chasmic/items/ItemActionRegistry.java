package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.items.actions.ItemWarpAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.network.packet.server.play.SetCooldownPacket;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ItemActionRegistry {
    private final Map<String, ItemAction> itemActions = new HashMap<>();

    public ItemActionRegistry() {
        // Register items actions here
        registerItemAction(new ItemAction("warp", 0, 20, new ItemWarpAction()));
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
            Date lastUsed = player.getItemCooldowns().get(actionId);
            if (lastUsed != null) {
                long timeSinceLastUse = new Date().getTime() - lastUsed.getTime();
                if (timeSinceLastUse < itemAction.cooldownSeconds() * 1000) {
                    player.sendMessage(Component.text("You must wait before using this item again!").color(NamedTextColor.RED));
                    return;
                }
            }

            // Check mana cost
            int playerMana = player.getCustomMana();
            if (playerMana < itemAction.manaCost()) {
                player.sendMessage(Component.text("You don't have enough mana to use this item!").color(NamedTextColor.RED));
                return;
            }

            // Execute the action
            ItemActionResult result = itemAction.actionHandler().handleAction(player);

            if (result.success) {
                // Deduct mana cost
                player.setCustomMana(playerMana - itemAction.manaCost());

                // Set cooldown
                player.usedItem(actionId);
            }
        });
    }

    public void registerItemAction(ItemAction itemAction) {
        itemActions.put(itemAction.id(), itemAction);
    }

    public ItemAction getItemAction(String id) {
        return itemActions.get(id);
    }
}
