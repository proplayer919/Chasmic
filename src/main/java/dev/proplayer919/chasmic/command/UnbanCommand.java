package dev.proplayer919.chasmic.command;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.punishment.Punishment;
import dev.proplayer919.chasmic.punishment.PunishmentManager;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

import java.util.UUID;

/**
 * /unban command for unbanning players
 * Permission: admin.command.unban
 */
public class UnbanCommand extends Command {
    @Setter
    private static PunishmentManager punishmentManager;

    public UnbanCommand() {
        super("unban");

        setCondition((sender, commandString) -> {
            if (sender instanceof CustomPlayer player) {
                if (!player.isInitialized()) {
                    return true;
                }
                return player.hasPermission("admin.command.unban");
            }
            return true; // Console always has permission
        });

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");

        // /unban <player> - Unban a player
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);

            // Try to find online player first
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);
            UUID targetUuid;

            if (target instanceof CustomPlayer customTarget) {
                targetUuid = customTarget.getUuid();
            } else {
                // Player not online, try to find in punishment history
                // This is a simplified approach - you might want to add a player lookup system
                sender.sendMessage(Component.text("Player must be specified by UUID when offline. Looking for recent bans...", NamedTextColor.YELLOW));

                // For now, we'll just show an error
                sender.sendMessage(Component.text("Player not found! They must be online or you need to use /unbanid <uuid>", NamedTextColor.RED));
                return;
            }

            // Check for active ban
            Punishment activeBan = punishmentManager.getActiveBan(targetUuid);

            if (activeBan == null) {
                sender.sendMessage(Component.text("This player is not banned!", NamedTextColor.RED));
                return;
            }

            // Deactivate the ban
            punishmentManager.deactivatePunishment(activeBan.getId());

            String adminName = sender instanceof Player p ? p.getUsername() : "Console";

            // Notify sender
            sender.sendMessage(Component.text("✔ ", NamedTextColor.GREEN)
                    .append(Component.text("Unbanned ", NamedTextColor.YELLOW))
                    .append(Component.text(targetName, NamedTextColor.GOLD)));

            // Notify all online admins
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                if (player instanceof CustomPlayer admin && admin.hasPermission("admin.notifications") && !admin.equals(sender)) {
                    admin.sendMessage(Component.text("[ADMIN] ", NamedTextColor.GREEN, TextDecoration.BOLD)
                            .append(Component.text(adminName, NamedTextColor.GOLD))
                            .append(Component.text(" unbanned ", NamedTextColor.YELLOW))
                            .append(Component.text(targetName, NamedTextColor.GOLD)));
                }
            }

        }, playerArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /unban <player>", NamedTextColor.RED)));
    }

    private boolean checkPermission(CommandSender sender) {
        if (sender instanceof CustomPlayer player) {
            if (!player.hasPermission("admin.command.unban")) {
                sender.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                return false;
            }
        }
        return true;
    }
}

