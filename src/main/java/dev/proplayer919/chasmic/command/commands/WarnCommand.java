package dev.proplayer919.chasmic.command.commands;

import dev.proplayer919.chasmic.CustomPlayer;
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
 * /warn command for warning players
 * Permission: admin.command.warn
 */
public class WarnCommand extends PermissionCommand {
    @Setter
    private static PunishmentManager punishmentManager;

    public WarnCommand() {
        super("warn", "admin.command.warn");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");
        ArgumentStringArray reasonArg = ArgumentType.StringArray("reason");

        // /warn <player> <reason> - Warn a player
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

            // Create punishment
            Punishment punishment = new Punishment();
            punishment.setId(punishmentManager.generatePunishmentId());
            punishment.setPlayerUuid(customTarget.getUuid());
            punishment.setPlayerName(customTarget.getUsername());
            punishment.setType(PunishmentType.WARNING);
            punishment.setReason(reason);
            punishment.setIssuedBy(sender instanceof Player p ? p.getUsername() : "Console");
            punishment.setIssuedAt(System.currentTimeMillis());
            punishment.setExpiresAt(-1); // Warnings don't expire
            punishment.setActive(true);

            // Save to database
            punishmentManager.savePunishment(punishment);

            // Send warning message to player using utility
            customTarget.sendMessage(Component.empty());
            customTarget.sendMessage(PunishmentMessages.createWarningMessage(punishment));
            customTarget.sendMessage(Component.empty());

            // Notify sender
            sender.sendMessage(Component.text("✔ ", NamedTextColor.GREEN)
                    .append(Component.text("Warned ", NamedTextColor.YELLOW))
                    .append(Component.text(customTarget.getUsername(), NamedTextColor.GOLD))
                    .append(Component.text(" for: ", NamedTextColor.YELLOW))
                    .append(Component.text(reason, NamedTextColor.WHITE)));

            // Notify all online admins
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                if (player instanceof CustomPlayer admin && admin.hasPermission("admin.notifications") && !admin.equals(sender)) {
                    admin.sendMessage(Component.text("[ADMIN] ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text(punishment.getIssuedBy(), NamedTextColor.GOLD))
                            .append(Component.text(" warned ", NamedTextColor.YELLOW))
                            .append(Component.text(customTarget.getUsername(), NamedTextColor.GOLD))
                            .append(Component.text(" for: ", NamedTextColor.YELLOW))
                            .append(Component.text(reason, NamedTextColor.GRAY)));
                }
            }

        }, playerArg, reasonArg);

        setDefaultExecutor((sender, context) -> sender.sendMessage(Component.text("Usage: /warn <player> <reason>", NamedTextColor.RED)));
    }
}




