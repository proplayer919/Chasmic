package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

/**
 * /heal command for fully healing the player
 * Permission: admin.command.heal
 */
public class HealCommand extends PermissionCommand {

    public HealCommand() {
        super("heal", "admin.command.heal");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        // /heal - Heal self
        setDefaultExecutor((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;
            player.setCustomHealth(player.getMaxCustomHealth());
            player.setCustomMana(player.getMaxCustomMana());
        });

        // /heal <player> - Heal another player
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            customTarget.setCustomHealth(customTarget.getMaxCustomHealth());
            customTarget.setCustomMana(customTarget.getMaxCustomMana());
        }, playerArg);
    }
}

