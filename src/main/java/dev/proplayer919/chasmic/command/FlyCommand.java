package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

/**
 * /fly command for toggling flight mode
 * Permission: admin.command.fly
 */
public class FlyCommand extends Command {

    public FlyCommand() {
        super("fly");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.fly");
            }
            return true; // Console always has permission
        });

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        // /fly - Toggle own flight
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

            if (!player.hasPermission("admin.command.fly")) {
                sender.sendMessage(Component.text("You don't have permission to use this command").color(NamedTextColor.RED));
                return;
            }

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

