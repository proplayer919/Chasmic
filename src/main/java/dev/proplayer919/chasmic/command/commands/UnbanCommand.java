package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.player.CustomPlayer;
import dev.proplayer919.chasmic.Emojis;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.player.punishment.Punishment;
import dev.proplayer919.chasmic.player.punishment.PunishmentManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;

import java.util.UUID;

/**
 * /unban command for unbanning players
 * Permission: admin.command.unban
 */
public class UnbanCommand extends PermissionCommand {
    public UnbanCommand(PunishmentManager punishmentManager) {
        super("unban", "admin.command.unban");

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
            sender.sendMessage(Component.text(Emojis.CHECKMARK.getEmoji(), NamedTextColor.GREEN)
                    .append(Component.text(" Unbanned ", NamedTextColor.YELLOW))
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
}

