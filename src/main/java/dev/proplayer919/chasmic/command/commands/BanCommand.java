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
import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentStringArray;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

import java.util.concurrent.TimeUnit;

/**
 * /ban command for banning players (permanent or temporary)
 * Permission: admin.command.ban
 */
public class BanCommand extends PermissionCommand {
    @Setter
    private static PunishmentManager punishmentManager;

    public BanCommand() {
        super("ban", "admin.command.ban");

        // Arguments
        PlayerNameArgument playerArg = PlayerNameArgument.playerName("player");
        ArgumentString durationArg = ArgumentType.String("duration"); // e.g., "1d", "30m", "permanent"
        ArgumentStringArray reasonArg = ArgumentType.StringArray("reason");

        // /ban <player> <duration> <reason> - Ban a player
        addSyntax((sender, context) -> {
            if (!checkPermission(sender)) return;

            String targetName = context.get(playerArg);
            String duration = context.get(durationArg);
            String[] reasonArray = context.get(reasonArg);
            String reason = String.join(" ", reasonArray);

            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(targetName);

            if (!(target instanceof CustomPlayer customTarget)) {
                sender.sendMessage(Component.text("Player not found!", NamedTextColor.RED));
                return;
            }

            String adminName = sender instanceof Player p ? p.getUsername() : "Console";

            // Parse duration
            long expiresAt;
            String durationDisplay;

            if (duration.equalsIgnoreCase("permanent") || duration.equalsIgnoreCase("perm")) {
                expiresAt = -1;
                durationDisplay = "Permanent";
            } else {
                try {
                    long durationMs = parseDuration(duration);
                    expiresAt = System.currentTimeMillis() + durationMs;
                    durationDisplay = formatDuration(durationMs);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(Component.text("Invalid duration format! Use: 1m, 1h, 1d, 1w, or 'permanent'", NamedTextColor.RED));
                    return;
                }
            }

            // Create punishment record
            Punishment punishment = new Punishment();
            punishment.setId(punishmentManager.generatePunishmentId());
            punishment.setPlayerUuid(customTarget.getUuid());
            punishment.setPlayerName(customTarget.getUsername());
            punishment.setType(PunishmentType.BAN);
            punishment.setReason(reason);
            punishment.setIssuedBy(adminName);
            punishment.setIssuedAt(System.currentTimeMillis());
            punishment.setExpiresAt(expiresAt);
            punishment.setActive(true);

            // Save to database
            punishmentManager.savePunishment(punishment);

            // Create ban screen using utility
            Component banMessage = PunishmentMessages.createBanMessage(punishment);

            // Kick the player with ban message
            customTarget.kick(banMessage);

            // Notify sender
            sender.sendMessage(Component.text(Emojis.CHECKMARK.getEmoji(), NamedTextColor.GREEN)
                    .append(Component.text(" Banned ", NamedTextColor.YELLOW))
                    .append(Component.text(customTarget.getUsername(), NamedTextColor.GOLD))
                    .append(Component.text(" for ", NamedTextColor.YELLOW))
                    .append(Component.text(durationDisplay, NamedTextColor.RED))
                    .append(Component.text(" - Reason: ", NamedTextColor.YELLOW))
                    .append(Component.text(reason, NamedTextColor.WHITE)));

            // Notify all online admins
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                if (player instanceof CustomPlayer admin && admin.hasPermission("admin.notifications") && !admin.equals(sender)) {
                    admin.sendMessage(Component.text("[ADMIN] ", NamedTextColor.RED, TextDecoration.BOLD)
                            .append(Component.text(adminName, NamedTextColor.GOLD))
                            .append(Component.text(" banned ", NamedTextColor.YELLOW))
                            .append(Component.text(customTarget.getUsername(), NamedTextColor.GOLD))
                            .append(Component.text(" for " + durationDisplay, NamedTextColor.YELLOW))
                            .append(Component.text(" - ", NamedTextColor.GRAY))
                            .append(Component.text(reason, NamedTextColor.GRAY)));
                }
            }

        }, playerArg, durationArg, reasonArg);

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(Component.text("Usage: /ban <player> <duration> <reason>", NamedTextColor.RED));
            sender.sendMessage(Component.text("Duration examples: 30m, 1h, 1d, 1w, permanent", NamedTextColor.GRAY));
        });
    }


    /**
     * Parse duration string (e.g., "1d", "30m", "2h") to milliseconds
     */
    private long parseDuration(String duration) throws IllegalArgumentException {
        if (duration == null || duration.length() < 2) {
            throw new IllegalArgumentException("Invalid duration");
        }

        String numberPart = duration.substring(0, duration.length() - 1);
        char unit = duration.charAt(duration.length() - 1);

        try {
            long amount = Long.parseLong(numberPart);

            return switch (unit) {
                case 's' -> TimeUnit.SECONDS.toMillis(amount);
                case 'm' -> TimeUnit.MINUTES.toMillis(amount);
                case 'h' -> TimeUnit.HOURS.toMillis(amount);
                case 'd' -> TimeUnit.DAYS.toMillis(amount);
                case 'w' -> TimeUnit.DAYS.toMillis(amount * 7);
                default -> throw new IllegalArgumentException("Invalid time unit: " + unit);
            };
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration number");
        }
    }

    /**
     * Format duration in milliseconds to human-readable string
     */
    private String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long weeks = days / 7;

        if (weeks > 0) {
            return weeks + " week" + (weeks > 1 ? "s" : "");
        } else if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "");
        }
    }
}




