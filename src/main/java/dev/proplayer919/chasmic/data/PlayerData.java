package dev.proplayer919.chasmic.data;

import dev.proplayer919.chasmic.PlayerRank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents player data stored in MongoDB
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerData {
    @BsonId
    private UUID uuid;

    @BsonProperty("username")
    private String username;

    @BsonProperty("rank")
    private String rankId;

    @BsonProperty("firstJoin")
    private long firstJoinTimestamp;

    @BsonProperty("lastJoin")
    private long lastJoinTimestamp;

    @BsonProperty("isNew")
    private boolean isNew;

    @BsonProperty("permissions")
    private List<String> customPermissions = new ArrayList<>();

    @BsonProperty("maxHealth")
    private int maxHealth = 100;

    @BsonProperty("maxMana")
    private int maxMana = 100;

    // Combat Stats
    @BsonProperty("attack")
    private float attack = 1.0f; // Attack damage multiplier

    @BsonProperty("defense")
    private float defense = 0.0f; // Defense damage reduction (0-1, where 0.5 = 50% reduction)

    @BsonProperty("criticalChance")
    private float criticalChance = 0.0f; // Critical chance multiplier (0-1, where 0.1 = 10% chance)

    /**
     * Gets the PlayerRank enum from the rank ID
     */
    @BsonIgnore
    public PlayerRank getRank() {
        if (rankId == null) {
            return PlayerRank.DEFAULT;
        }

        for (PlayerRank rank : PlayerRank.values()) {
            if (rank.getId().equals(rankId)) {
                return rank;
            }
        }

        return PlayerRank.DEFAULT;
    }

    /**
     * Sets the rank from a PlayerRank enum
     */
    @BsonIgnore
    public void setRank(PlayerRank rank) {
        this.rankId = rank.getId();
    }
}

