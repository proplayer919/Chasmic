package dev.proplayer919.chasmic.data;

import dev.proplayer919.chasmic.PlayerRank;
import dev.proplayer919.chasmic.accessories.AccessoryBag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents player data stored in MongoDB
 */
@Getter
@Setter
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

    // EXP
    @BsonProperty("currentExp")
    private long currentExp = 0; // Current EXP points

    // Currencies
    @BsonProperty("purse")
    private long purse = 0; // In-game currency

    @BsonProperty("bank")
    private long bank = 0; // Banked currency

    // Other currencies
    @BsonProperty("riftGold")
    private long riftGold = 0; // Rift Gold currency

    @BsonProperty("upgradePoints")
    private long upgradePoints = 0; // Upgrade points for character progression

    @BsonProperty("shards")
    private long shards = 0; // Shards currency for special purchases

    // Accessory data
    @BsonProperty
    private int accessorySlots = 3; // Number of accessory slots available to the player

    @BsonIgnore
    private List<String> accessoryIds = new ArrayList<>(); // List of accessory IDs in the player's bag

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

    @BsonProperty("accessoryBag")
    public List<String> getAccessoryBag() {
        return accessoryIds;
    }

    @BsonProperty("accessoryBag")
    public void setAccessoryBag(List<String> accessoryBag) {
        this.accessoryIds = accessoryBag;
    }

    /**
     * Gets the AccessoryBag object from the stored accessory IDs
     */
    @BsonIgnore
    public AccessoryBag getAccessoryBagObject() {
        AccessoryBag bag = new AccessoryBag();
        if (accessoryIds != null) {
            bag.deserialize(accessoryIds);
        }
        return bag;
    }

    /**
     * Sets the rank from a PlayerRank enum
     */
    @BsonIgnore
    public void setRank(PlayerRank rank) {
        this.rankId = rank.getId();
    }
}

