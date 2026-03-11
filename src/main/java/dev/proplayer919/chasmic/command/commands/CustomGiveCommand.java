package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

/**
 * /cgive command for giving custom items to players
 * Permission: admin.command.cgive
 */
public class CustomGiveCommand extends PermissionCommand {
    @Setter
    private static CustomItemRegistry customItemRegistry;

    public CustomGiveCommand() {
        super("cgive", "admin.command.cgive");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        String[] allItems = customItemRegistry.getAllItems().stream().map(CustomItem::getId).toArray(String[]::new);
        ArgumentWord itemArg = new ArgumentWord("item").from(allItems);

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

            CustomItem customItem = customItemRegistry.getItem(context.get(itemArg));

            if (customItem == null) {
                sender.sendMessage(Component.text("Item not found!", NamedTextColor.RED));
                return;
            }

            int amount = Math.min(64, Math.max(1, context.get(amountArg)));

            // Create and give the item
            PlayerInventory inventory = target.getInventory();
            ItemStack itemStack = customItem.getItemStack(amount);
            inventory.addItemStack(itemStack);

            sender.sendMessage(Component.text("Gave " + amount + "x " + customItem.getDisplayName() + " to " + target.getUsername(), NamedTextColor.GREEN));
        }, playerArg, itemArg, amountArg);
    }
}

