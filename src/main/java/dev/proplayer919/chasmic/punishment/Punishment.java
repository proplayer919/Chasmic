package dev.proplayer919.chasmic.punishment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.UUID;

/**
 * Represents a punishment (warning, kick, or ban) issued to a player
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Punishment {
    @BsonProperty("id")
    private String id; // Unique punishment ID

    @BsonProperty("playerUuid")
    private UUID playerUuid;

    @BsonProperty("playerName")
    private String playerName;

    @BsonProperty("type")
    private PunishmentType type;

    @BsonProperty("reason")
    private String reason;

    @BsonProperty("issuedBy")
    private String issuedBy; // Name of admin who issued the punishment

    @BsonProperty("issuedAt")
    private long issuedAt; // Timestamp when punishment was issued

    @BsonProperty("expiresAt")
    private long expiresAt; // Timestamp when punishment expires (-1 for permanent)

    @BsonProperty("active")
    private boolean active = true;

    /**
     * Check if this punishment is currently active
     */
    public boolean isActive() {
        if (!active) {
            return false;
        }

        // Check if temporary ban has expired
        return expiresAt == -1 || System.currentTimeMillis() <= expiresAt;
    }

    /**
     * Get a human-readable duration string
     */
    public String getDurationString() {
        if (expiresAt == -1) {
            return "Permanent";
        }

        long durationMs = expiresAt - issuedAt;
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "");
        }
    }

    /**
     * Get remaining time string
     */
    public String getRemainingTimeString() {
        if (expiresAt == -1) {
            return "Never";
        }

        long remainingMs = expiresAt - System.currentTimeMillis();
        if (remainingMs <= 0) {
            return "Expired";
        }

        long seconds = remainingMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
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

