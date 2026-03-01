package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.items.CustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

/**
 * /cgive command for giving custom items to players
 * Permission: admin.command.cgive
 */
public class CustomGiveCommand extends Command {

    public CustomGiveCommand() {
        super("cgive");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.cgive");
            }
            return true; // Console always has permission
        });

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        ArgumentWord itemArg = new ArgumentWord("item").from(Main.getCustomItemRegistry().getAllItems().stream().map(CustomItem::getId).toArray(String[]::new));

        ArgumentInteger amountArg = (ArgumentInteger) ArgumentType.Integer("amount").setDefaultValue(1);

        // /cgive <player> <item> [amount] - Give item to player
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            CustomItem customItem = Main.getCustomItemRegistry().getItem(context.get(itemArg));
            int amount = Math.min(64, Math.max(1, context.get(amountArg)));

            if (customItem == null) {
                sender.sendMessage(Component.text("Custom item not found!", NamedTextColor.RED));
                return;
            }

            // Create and give the item
            PlayerInventory inventory = target.getInventory();
            ItemStack itemStack = customItem.getItemStack(amount);
            inventory.addItemStack(itemStack);

            target.sendMessage(Component.text("Received " + amount + "x ", NamedTextColor.GREEN)
                    .append(Component.text(customItem.getDisplayName(), NamedTextColor.AQUA)));

            sender.sendMessage(Component.text("Gave " + amount + "x " + customItem.getDisplayName() + " to " + target.getUsername(), NamedTextColor.GREEN));
        }, playerArg, itemArg, amountArg);
    }
}


