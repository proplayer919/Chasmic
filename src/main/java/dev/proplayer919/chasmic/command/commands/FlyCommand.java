package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

/**
 * /fly command for toggling flight mode
 * Permission: admin.command.fly
 */
public class FlyCommand extends PermissionCommand {

    public FlyCommand() {
        super("fly", "admin.command.fly");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        // /fly - Toggle own flight
        setDefaultExecutor((sender, context) -> {
            if (!checkPlayerPermission(sender)) return;

            CustomPlayer player = (CustomPlayer) sender;

            boolean newFlyState = !player.isAllowFlying();
            player.setAllowFlying(newFlyState);

            if (newFlyState) {
                sender.sendMessage(Component.text("Flight mode enabled!", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("Flight mode disabled.", NamedTextColor.GRAY));
                // Disable flying state if player is currently flying
                if (player.isFlying()) {
                    player.setFlying(false);
                }
            }
        });

        // /fly <player> - Toggle flight for another player
        addSyntax((sender, context) -> {
            String targetName = context.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            boolean newFlyState = !customTarget.isAllowFlying();
            customTarget.setAllowFlying(newFlyState);

            if (newFlyState) {
                target.sendMessage(Component.text("Flight mode enabled!", NamedTextColor.GREEN));
            } else {
                target.sendMessage(Component.text("Flight mode disabled.", NamedTextColor.GRAY));
                if (customTarget.isFlying()) {
                    customTarget.setFlying(false);
                }
            }

            sender.sendMessage(Component.text(target.getUsername() + "'s flight mode: ", NamedTextColor.YELLOW)
                    .append(Component.text(newFlyState ? "enabled" : "disabled", newFlyState ? NamedTextColor.GREEN : NamedTextColor.GRAY)));
        }, playerArg);
    }
}

