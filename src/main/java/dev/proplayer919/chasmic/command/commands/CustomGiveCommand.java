package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.accessories.Accessory;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.items.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * /cgive command for giving custom items and accessories to players
 * Permission: admin.command.cgive
 */
public class CustomGiveCommand extends PermissionCommand {

    public CustomGiveCommand() {
        super("cgive", "admin.command.cgive");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        String[] allItems = Main.getCustomItemRegistry().getAllItems().stream().map(CustomItem::getId).toArray(String[]::new);
        String[] allAccessories = Main.getAccessoryRegistry().getAllAccessories().stream().map(Accessory::getId).toArray(String[]::new);
        List<String> both = new ArrayList<>();
        both.addAll(List.of(allItems));
        both.addAll(List.of(allAccessories));

        ArgumentWord itemArg = new ArgumentWord("item").from(both.toArray(new String[0]));

        ArgumentInteger amountArg = (ArgumentInteger) ArgumentType.Integer("amount").setDefaultValue(1);

        // /cgive <player> <item> [amount] - Give item to player
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            CustomItem customItem = Main.getCustomItemRegistry().getItem(context.get(itemArg));
            Accessory accessory = Main.getAccessoryRegistry().getAccessoryById(context.get(itemArg));

            int amount = Math.min(64, Math.max(1, context.get(amountArg)));

            if (customItem == null) {
                // Create and give the item
                PlayerInventory inventory = target.getInventory();
                ItemStack itemStack = accessory.getItemStack(amount);
                inventory.addItemStack(itemStack);

                target.sendMessage(Component.text("Received " + amount + "x ", NamedTextColor.GREEN)
                        .append(Component.text(accessory.getName(), NamedTextColor.AQUA)));

                sender.sendMessage(Component.text("Gave " + amount + "x " + accessory.getName() + " to " + target.getUsername(), NamedTextColor.GREEN));
            } else if (accessory == null) {
                // Create and give the item
                PlayerInventory inventory = target.getInventory();
                ItemStack itemStack = customItem.getItemStack(amount);
                inventory.addItemStack(itemStack);

                target.sendMessage(Component.text("Received " + amount + "x ", NamedTextColor.GREEN)
                        .append(Component.text(customItem.getDisplayName(), NamedTextColor.AQUA)));

                sender.sendMessage(Component.text("Gave " + amount + "x " + customItem.getDisplayName() + " to " + target.getUsername(), NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Custom item not found!", NamedTextColor.RED));
            }
        }, playerArg, itemArg, amountArg);
    }
}


