package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.entity.EntityFinder;

/**
 * /give command for giving items to players
 * Permission: admin.command.give
 */
public class GiveCommand extends Command {

    public GiveCommand() {
        super("give");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                return player.hasPermission("admin.command.give");
            }
            return true; // Console always has permission
        });

        // Arguments
        ArgumentEntity playerArg = ArgumentType.Entity("player")
                .onlyPlayers(true)
                .singleEntity(true);

        ArgumentItemStack itemArg = ArgumentType.ItemStack("item");

        ArgumentInteger amountArg = (ArgumentInteger) ArgumentType.Integer("amount").setDefaultValue(1);

        // /give <player> <item> [amount] - Give item to player
        addSyntax((sender, context) -> {
            EntityFinder finder = context.get(playerArg);
            Player target = finder.findFirstPlayer(sender);

            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
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


