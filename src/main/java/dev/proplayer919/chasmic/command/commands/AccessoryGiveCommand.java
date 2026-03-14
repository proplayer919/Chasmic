package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.accessories.Accessory;
import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

/**
 * /agive command for giving accessories to players
 * Permission: admin.command.agive
 */
public class AccessoryGiveCommand extends PermissionCommand {
    public AccessoryGiveCommand(AccessoryRegistry accessoryRegistry) {
        super("agive", "admin.command.agive");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        String[] allAccessories = accessoryRegistry.getAllAccessories().stream().map(Accessory::getId).toArray(String[]::new);
        ArgumentWord accessoryArg = new ArgumentWord("accessory").from(allAccessories);

        // /agive <player> <accessory> - Give accessory to player
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            Accessory accessory = accessoryRegistry.getAccessoryById(context.get(accessoryArg));

            if (accessory == null) {
                sender.sendMessage(Component.text("Accessory not found!", NamedTextColor.RED));
                return;
            }

            // Create and give the item
            PlayerInventory inventory = target.getInventory();
            ItemStack itemStack = accessory.getItemStack();
            inventory.addItemStack(itemStack);

            sender.sendMessage(Component.text("Gave a " + accessory.getName() + " to " + targetName, NamedTextColor.GREEN));
        }, playerArg, accessoryArg);
    }
}


