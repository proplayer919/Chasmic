package dev.proplayer919.chasmic.service.module;


import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.items.ItemType;
import dev.proplayer919.chasmic.service.Module;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class ItemFoodModule implements Module {
    @Override
    public void attach(@NonNull EventNode<Event> eventNode) {
        // Register a right click listener to handle item actions
        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            if (!(event.getPlayer() instanceof CustomPlayer player)) {
                return;
            }

            // Check if the item is edible and is a custom item
            String itemId = event.getItemStack().getTag(CustomItem.itemIdTag);

            if (itemId == null) {
                return;
            }

            CustomItem customItem = Main.getServiceContainer().getCustomItemRegistry().getItem(itemId);

            if (customItem == null) {
                return;
            }

            if (!customItem.getItemType().equals(ItemType.FOOD)) {
                return;
            }

            // Handle the food
            int healAmount = customItem.getHealthRestoration();

            int newHealth = Math.min(player.getCustomHealth() + healAmount, player.getMaxCustomHealth());

            player.setCustomHealth(newHealth);

            // Play eating sound
            player.playSound(Sound.sound(Key.key("minecraft:entity.player.burp"), Sound.Source.PLAYER, 1f, 1f));

            // Remove one item from the stack
            PlayerInventory inventory = player.getInventory();

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack stack = inventory.getItemStack(slot);
                String slotItemId = stack.getTag(CustomItem.itemIdTag);

                if (slotItemId == null) {
                    continue;
                }

                if (slotItemId.equals(itemId)) {
                    int newAmount = stack.amount() - 1;
                    if (newAmount <= 0) {
                        inventory.setItemStack(slot, ItemStack.AIR);
                    } else {
                        ItemStack newStack = stack.withAmount(stack.amount() - 1);
                        inventory.setItemStack(slot, newStack);
                    }

                    player.persistActiveProfileInventoryIfChanged();
                    break;
                }
            }
        });
    }

    @Override
    public String getName() {
        return "ItemFoodModule";
    }
}
