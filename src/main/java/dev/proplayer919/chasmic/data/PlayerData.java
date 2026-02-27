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

