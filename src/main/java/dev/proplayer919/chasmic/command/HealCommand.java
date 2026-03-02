package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

/**
 * /heal command for fully healing the player
 * Permission: admin.command.heal
 */
public class HealCommand extends Command {

    public HealCommand() {
        super("heal");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                // Allow command if player is not yet initialized (to avoid red text)
                // Actual permission check happens in executor
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.heal");
            }
            return true; // Console always has permission
        });

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        // /heal - Heal self
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CustomPlayer player)) {
                sender.sendMessage(Component.text("Only players can use this command!", NamedTextColor.RED));
                return;
            }

            if (!player.hasPermission("admin.command.heal")) {
                sender.sendMessage(Component.text("You don't have permission to use this command").color(NamedTextColor.RED));
                return;
            }

            player.setCustomHealth(player.getMaxCustomHealth());
            player.setCustomMana(player.getMaxCustomMana());
        });

        // /heal <player> - Heal another player
        addSyntax((sender, context) -> {
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

