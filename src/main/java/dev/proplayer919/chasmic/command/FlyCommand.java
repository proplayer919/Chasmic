package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;

/**
 * /fly command for toggling flight mode
 * Permission: admin.command.fly
 */
public class FlyCommand extends Command {

    public FlyCommand() {
        super("fly");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                return player.hasPermission("admin.command.fly");
            }
            return true; // Console always has permission
        });

        // Arguments
        ArgumentEntity playerArg = ArgumentType.Entity("player")
                .onlyPlayers(true)
                .singleEntity(true);

        // /fly - Toggle own flight
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
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
            EntityFinder finder = context.get(playerArg);
            Player target = finder.findFirstPlayer(sender);

            if (target == null) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            CustomPlayer customTarget = (CustomPlayer) target;
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

