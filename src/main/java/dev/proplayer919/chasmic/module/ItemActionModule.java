package dev.proplayer919.chasmic.module;


import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.items.ItemAction;
import dev.proplayer919.chasmic.items.ItemActionResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class ItemActionModule implements Module {
    @Override
    public void attach(@NotNull EventNode<Event> eventNode) {
        // Register a right click listener to handle item actions
        eventNode.addListener(PlayerUseItemEvent .class, event -> {
            if (!(event.getPlayer() instanceof CustomPlayer player)) {
                return;
            }

            // Check if the item has an action
            String actionId = event.getItemStack().getTag(CustomItem.itemActionTag);

            if (actionId == null) {
                return;
            }

            ItemAction itemAction = Main.getItemActionRegistry().getItemAction(actionId);

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

            if (result.success()) {
                // Deduct mana cost
                player.setCustomMana(playerMana - itemAction.manaCost());

                // Set cooldown
                player.usedItem(actionId);
            }
        });
    }

    @Override
    public String getName() {
        return "TabListModule";
    }
}
