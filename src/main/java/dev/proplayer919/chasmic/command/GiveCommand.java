package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

/**
 * /give command for giving items to players
 * Permission: admin.command.give
 */
public class GiveCommand extends Command {

    public GiveCommand() {
        super("give");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.give");
            }
            return true; // Console always has permission
        });

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        ArgumentItemStack itemArg = ArgumentType.ItemStack("item");

        ArgumentInteger amountArg = (ArgumentInteger) ArgumentType.Integer("amount").setDefaultValue(1);

        // /give <player> <item> [amount] - Give item to player
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            if ((sender instanceof CustomPlayer) && !((CustomPlayer) sender).hasPermission("admin.command.gamemode")) {
                sender.sendMessage(Component.text("You don't have permission to use this command").color(NamedTextColor.RED));
                return;
            }

            ItemStack item = context.get(itemArg);
            int amount = Math.min(64, Math.max(1, context.get(amountArg)));

            // Try to parse the item
            Material material = item.material();
            String materialName = material.name().split(":")[1]; // Get the material name without namespace

            // Create and give the item
            PlayerInventory inventory = target.getInventory();
            ItemStack itemStack = ItemStack.of(material, (byte) amount);
            inventory.addItemStack(itemStack);

            target.sendMessage(Component.text("Received " + amount + "x ", NamedTextColor.GREEN)
                    .append(Component.text(materialName, NamedTextColor.AQUA)));

            sender.sendMessage(Component.text("Gave " + amount + "x " + materialName + " to " + target.getUsername(), NamedTextColor.GREEN));
        }, playerArg, itemArg, amountArg);
    }
}


