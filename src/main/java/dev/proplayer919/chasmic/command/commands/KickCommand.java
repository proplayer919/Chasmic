package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
import dev.proplayer919.chasmic.Emojis;
import dev.proplayer919.chasmic.command.PermissionCommand;
import dev.proplayer919.chasmic.command.PlayerNameArgument;
import dev.proplayer919.chasmic.punishment.Punishment;
import dev.proplayer919.chasmic.punishment.PunishmentManager;
import dev.proplayer919.chasmic.punishment.PunishmentMessages;
import dev.proplayer919.chasmic.punishment.PunishmentType;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

/**
 * /kick command for kicking players
 * Permission: admin.command.kick
 */
public class KickCommand extends PermissionCommand {
    @Setter
    private static PunishmentManager punishmentManager;

    public KickCommand() {
        super("kick", "admin.command.kick");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");
        ArgumentStringArray reasonArg = ArgumentType.StringArray("reason");

        // /kick <player> <reason> - Kick a player
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            String[] reasonArray = context.get(reasonArg);
            String reason = String.join(" ", reasonArray);

            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            String adminName = sender instanceof Player p ? p.getUsername() : "Console";

            // Create punishment record
            Punishment punishment = new Punishment();
            punishment.setId(punishmentManager.generatePunishmentId());
            punishment.setPlayerUuid(customTarget.getUuid());
            punishment.setPlayerName(customTarget.getUsername());
            punishment.setType(PunishmentType.KICK);
            punishment.setReason(reason);
            punishment.setIssuedBy(adminName);
            punishment.setIssuedAt(System.currentTimeMillis());
            punishment.setExpiresAt(-1);
            punishment.setActive(true);

            // Save to database
            punishmentManager.savePunishment(punishment);

            // Create kick screen using utility
            Component kickMessage = PunishmentMessages.createKickMessage(punishment);

            // Kick the player
            customTarget.kick(kickMessage);

            // Notify sender
            sender.sendMessage(Component.text(Emojis.CHECKMARK.getEmoji(), NamedTextColor.GREEN)
                    .append(Component.text(" Kicked ", NamedTextColor.YELLOW))
                    .append(Component.text(customTarget.getUsername(), NamedTextColor.GOLD))
                    .append(Component.text(" for: ", NamedTextColor.YELLOW))
                    .append(Component.text(reason, NamedTextColor.WHITE)));

            // Notify all online admins
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                if (player instanceof CustomPlayer admin && admin.hasPermission("admin.notifications") && !admin.equals(sender)) {
                    admin.sendMessage(Component.text("[ADMIN] ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text(adminName, NamedTextColor.GOLD))
                            .append(Component.text(" kicked ", NamedTextColor.YELLOW))
                            .append(Component.text(customTarget.getUsername(), NamedTextColor.GOLD))
                            .append(Component.text(" for: ", NamedTextColor.YELLOW))
                            .append(Component.text(reason, NamedTextColor.GRAY)));
                }
            }

        }, playerArg, reasonArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /kick <player> <reason>", NamedTextColor.RED)));
    }
}




