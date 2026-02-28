package dev.proplayer919.chasmic.punishment;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for creating formatted punishment messages
 */
public class PunishmentMessages {
    private static final String BORDER = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final String BORDER_SMALL = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    /**
     * Create a formatted ban screen message
     */
    public static Component createBanMessage(Punishment punishment) {
        String durationDisplay = getDurationDisplay(punishment);

        Component message = Component.empty()
                .append(Component.text(BORDER + "\n", NamedTextColor.DARK_RED, TextDecoration.BOLD))
                .append(Component.text("\n"))
                .append(Component.text("❌ YOU ARE BANNED ❌\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("\n"))
                .append(Component.text("Reason: ", NamedTextColor.GRAY))
                .append(Component.text(punishment.getReason() + "\n", NamedTextColor.WHITE))
                .append(Component.text("Banned by: ", NamedTextColor.GRAY))
                .append(Component.text(punishment.getIssuedBy() + "\n", NamedTextColor.YELLOW))
                .append(Component.text("Duration: ", NamedTextColor.GRAY))
                .append(Component.text(durationDisplay + "\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("\n"));

        // Add expiration time if temporary ban
        if (punishment.getExpiresAt() != -1) {
            message = message.append(Component.text("Expires: ", NamedTextColor.GRAY))
                    .append(Component.text(DATE_FORMAT.format(new Date(punishment.getExpiresAt())) + "\n", NamedTextColor.YELLOW))
                    .append(Component.text("\n"));
        }

        message = message
                .append(Component.text("Appeal at: ", NamedTextColor.GRAY))
                .append(Component.text("discord.gg/yourserver\n", NamedTextColor.AQUA))
                .append(Component.text("\n"))
                .append(Component.text("Ban ID: ", NamedTextColor.DARK_GRAY))
                .append(Component.text("#" + punishment.getId() + "\n", NamedTextColor.GRAY))
                .append(Component.text(BORDER, NamedTextColor.DARK_RED, TextDecoration.BOLD));

        return message;
    }

    /**
     * Create a formatted kick screen message
     */
    public static Component createKickMessage(Punishment punishment) {
        return Component.empty()
                .append(Component.text(BORDER + "\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("\n"))
                .append(Component.text("⚠ KICKED FROM SERVER ⚠\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("\n"))
                .append(Component.text("Reason: ", NamedTextColor.GRAY))
                .append(Component.text(punishment.getReason() + "\n", NamedTextColor.WHITE))
                .append(Component.text("Kicked by: ", NamedTextColor.GRAY))
                .append(Component.text(punishment.getIssuedBy() + "\n", NamedTextColor.YELLOW))
                .append(Component.text("\n"))
                .append(Component.text("You may rejoin the server.\n", NamedTextColor.GREEN))
                .append(Component.text("\n"))
                .append(Component.text("Kick ID: ", NamedTextColor.DARK_GRAY))
                .append(Component.text("#" + punishment.getId() + "\n", NamedTextColor.GRAY))
                .append(Component.text(BORDER, NamedTextColor.RED, TextDecoration.BOLD));
    }

    /**
     * Create a formatted warning message for chat
     */
    public static Component createWarningMessage(Punishment punishment) {
        return Component.empty()
                .append(Component.text(BORDER_SMALL + "\n", NamedTextColor.RED, TextDecoration.BOLD))
                .append(Component.text("⚠ WARNING ⚠\n", NamedTextColor.GOLD, TextDecoration.BOLD))
                .append(Component.text("You have been warned!\n", NamedTextColor.YELLOW))
                .append(Component.text("\n"))
                .append(Component.text("Reason: ", NamedTextColor.GRAY))
                .append(Component.text(punishment.getReason() + "\n", NamedTextColor.WHITE))
                .append(Component.text("Issued by: ", NamedTextColor.GRAY))
                .append(Component.text(punishment.getIssuedBy() + "\n", NamedTextColor.YELLOW))
                .append(Component.text("\n"))
                .append(Component.text("Warning ID: ", NamedTextColor.DARK_GRAY))
                .append(Component.text("#" + punishment.getId() + "\n", NamedTextColor.GRAY))
                .append(Component.text(BORDER_SMALL, NamedTextColor.RED, TextDecoration.BOLD));
    }

    /**
     * Get human-readable duration display
     */
    private static String getDurationDisplay(Punishment punishment) {
        if (punishment.getExpiresAt() == -1) {
            return "Permanent";
        }

        long durationMs = punishment.getExpiresAt() - punishment.getIssuedAt();
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

