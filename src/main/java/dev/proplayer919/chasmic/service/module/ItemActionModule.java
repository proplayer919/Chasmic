package dev.proplayer919.chasmic.service.module;


import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.items.ItemAction;
import dev.proplayer919.chasmic.items.ItemActionRegistry;
import dev.proplayer919.chasmic.items.ItemActionResult;
import dev.proplayer919.chasmic.items.ItemActionType;
import dev.proplayer919.chasmic.service.Module;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemActionModule implements Module {
    private final ItemActionRegistry itemActionRegistry;

    public ItemActionModule(ItemActionRegistry itemActionRegistry) {
        this.itemActionRegistry = itemActionRegistry;
    }

    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        // Register a right click listener to handle item actions
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (!(event.getPlayer() instanceof CustomPlayer player)) {
                return;
            }

            // Check if the item has an action
            String itemActions = event.getItemStack().getTag(CustomItem.itemActionsTag);

            if (itemActions == null) {
                return;
            }

            List<ItemAction> actions = new ArrayList<>();
            for (String actionId : itemActions.split(",")) {
                ItemAction action = itemActionRegistry.getItemAction(actionId);
                if (action != null) {
                    actions.add(action);
                }
            }

            ItemActionType actionType = event.getPlayer().isSneaking() ? ItemActionType.SHIFT_RIGHT_CLICK : ItemActionType.RIGHT_CLICK;

            // Find the first action that matches the click type
            ItemAction itemAction = actions.stream()
                    .filter(action -> action.actionType() == actionType)
                    .findFirst()
                    .orElse(null);

            if (itemAction == null) {
                return; // No action for this click type
            }

            // Check cooldown
            Date lastUsed = player.getItemCooldowns().get(itemAction.id());
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

            if (result.success()) {
                // Deduct mana cost
                player.setCustomMana(playerMana - itemAction.manaCost());

                // Set cooldown
                player.usedItem(itemAction.id());
            }
        });

        // Stop players from dropping items
        eventNode.addListener(ItemDropEvent.class, event -> {
            if (event.getPlayer() instanceof CustomPlayer) {
                event.setCancelled(true);
            }
        });
    }

    @Override
    public String getName() {
        return "ItemActionModule";
    }
}
