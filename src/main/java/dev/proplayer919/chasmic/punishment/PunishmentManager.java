package dev.proplayer919.chasmic.punishment;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages punishment storage and retrieval
 */
@Getter
public class PunishmentManager {
    private final MongoDBHandler mongoDBHandler;
    private MongoCollection<Document> punishmentCollection;

    public PunishmentManager(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
        initializeCollection();
    }

    private void initializeCollection() {
        MongoDatabase database = mongoDBHandler.getDatabase();
        punishmentCollection = database.getCollection("punishments");
    }

    /**
     * Save a punishment to the database
     */
    public void savePunishment(Punishment punishment) {
        Document doc = new Document()
                .append("id", punishment.getId())
                .append("playerUuid", punishment.getPlayerUuid().toString())
                .append("playerName", punishment.getPlayerName())
                .append("type", punishment.getType().name())
                .append("reason", punishment.getReason())
                .append("issuedBy", punishment.getIssuedBy())
                .append("issuedAt", punishment.getIssuedAt())
                .append("expiresAt", punishment.getExpiresAt())
                .append("active", punishment.isActive());

        punishmentCollection.insertOne(doc);
    }

    /**
     * Get active ban for a player
     */
    public Punishment getActiveBan(UUID playerUuid) {
        Document doc = punishmentCollection.find(
                Filters.and(
                        Filters.eq("playerUuid", playerUuid.toString()),
                        Filters.eq("type", PunishmentType.BAN.name()),
                        Filters.eq("active", true)
                )
        ).first();

        if (doc == null) {
            return null;
        }

        Punishment punishment = documentToPunishment(doc);

        // Check if ban has expired
        if (!punishment.isActive()) {
            // Deactivate expired ban
            deactivatePunishment(punishment.getId());
            return null;
        }

        return punishment;
    }

    /**
     * Get all punishments for a player
     */
    public List<Punishment> getPlayerPunishments(UUID playerUuid) {
        List<Punishment> punishments = new ArrayList<>();

        for (Document doc : punishmentCollection.find(Filters.eq("playerUuid", playerUuid.toString()))) {
            punishments.add(documentToPunishment(doc));
        }

        return punishments;
    }

    /**
     * Get active warnings for a player
     */
    public List<Punishment> getActiveWarnings(UUID playerUuid) {
        List<Punishment> warnings = new ArrayList<>();

        for (Document doc : punishmentCollection.find(
                Filters.and(
                        Filters.eq("playerUuid", playerUuid.toString()),
                        Filters.eq("type", PunishmentType.WARNING.name()),
                        Filters.eq("active", true)
                )
        )) {
            Punishment punishment = documentToPunishment(doc);
            if (punishment.isActive()) {
                warnings.add(punishment);
            }
        }

        return warnings;
    }

    /**
     * Deactivate a punishment
     */
    public void deactivatePunishment(String punishmentId) {
        punishmentCollection.updateOne(
                Filters.eq("id", punishmentId),
                Updates.set("active", false)
        );
    }

    /**
     * Convert a MongoDB Document to a Punishment object
     */
    private Punishment documentToPunishment(Document doc) {
        Punishment punishment = new Punishment();
        punishment.setId(doc.getString("id"));
        punishment.setPlayerUuid(UUID.fromString(doc.getString("playerUuid")));
        punishment.setPlayerName(doc.getString("playerName"));
        punishment.setType(PunishmentType.valueOf(doc.getString("type")));
        punishment.setReason(doc.getString("reason"));
        punishment.setIssuedBy(doc.getString("issuedBy"));
        punishment.setIssuedAt(doc.getLong("issuedAt"));
        punishment.setExpiresAt(doc.getLong("expiresAt"));
        punishment.setActive(doc.getBoolean("active", true));
        return punishment;
    }

    /**
     * Generate a unique punishment ID
     */
    public String generatePunishmentId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

