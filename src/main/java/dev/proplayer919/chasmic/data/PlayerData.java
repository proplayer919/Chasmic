package dev.proplayer919.chasmic.data;

import dev.proplayer919.chasmic.accessories.AccessoryBag;
import dev.proplayer919.chasmic.player.PlayerRank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents player data stored in MongoDB.
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

    // Global data
    @BsonProperty("rank")
    private String rankId;

    @BsonProperty("permissions")
    private List<String> customPermissions = new ArrayList<>();

    @BsonProperty("shards")
    private long shards = 0;

    @BsonProperty("firstJoin")
    private long firstJoinTimestamp;

    @BsonProperty("lastJoin")
    private long lastJoinTimestamp;

    @BsonProperty("isNew")
    private boolean isNew;

    @BsonProperty("schemaVersion")
    private int schemaVersion = 2;

    // Profile system
    @BsonProperty("profiles")
    private List<PlayerProfileData> profiles = new ArrayList<>();

    @BsonProperty("activeProfileId")
    private String activeProfileId;

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

    @BsonIgnore
    public void setRank(PlayerRank rank) {
        this.rankId = rank.getId();
    }

    @BsonIgnore
    public PlayerProfileData getActiveProfile() {
        ensureProfileIntegrity();

        if (activeProfileId == null) {
            activeProfileId = profiles.getFirst().getProfileId();
        }

        for (PlayerProfileData profile : profiles) {
            if (profile.getProfileId().equals(activeProfileId)) {
                return profile;
            }
        }

        PlayerProfileData fallback = profiles.getFirst();
        activeProfileId = fallback.getProfileId();
        return fallback;
    }

    @BsonIgnore
    public void ensureProfileIntegrity() {
        if (profiles == null) {
            profiles = new ArrayList<>();
        }

        profiles.removeIf(profile -> profile == null || profile.getProfileId() == null || profile.getProfileId().isBlank());

        if (profiles.isEmpty()) {
            profiles.add(new PlayerProfileData(PlayerProfileData.buildIdForSlot(1)));
        }

        for (PlayerProfileData profile : profiles) {
            if (profile.getAccessoryIds() == null) {
                profile.setAccessoryIds(new ArrayList<>());
            }
            if (profile.getUnlockedLocationIds() == null) {
                profile.setUnlockedLocationIds(new ArrayList<>());
            }
            if (profile.getInventoryItems() == null) {
                profile.setInventoryItems(new ArrayList<>());
            } else {
                profile.getInventoryItems().removeIf(entry ->
                        entry == null
                                || entry.getItemId() == null
                                || entry.getItemId().isBlank()
                                || entry.getAmount() <= 0
                                || entry.getSlot() < PlayerProfileData.TRACKED_SLOT_MIN
                                || entry.getSlot() > PlayerProfileData.TRACKED_SLOT_MAX
                                || entry.getSlot() == PlayerProfileData.MENU_SLOT
                );
            }
        }

        if (activeProfileId == null || profiles.stream().noneMatch(profile -> profile.getProfileId().equals(activeProfileId))) {
            activeProfileId = profiles.getFirst().getProfileId();
        }
    }

    @BsonIgnore
    public boolean switchActiveProfile(String profileId) {
        ensureProfileIntegrity();
        for (PlayerProfileData profile : profiles) {
            if (profile.getProfileId().equals(profileId)) {
                activeProfileId = profileId;
                return true;
            }
        }
        return false;
    }

    @BsonIgnore
    public boolean hasProfile(String profileId) {
        ensureProfileIntegrity();
        return profiles.stream().anyMatch(profile -> profile.getProfileId().equals(profileId));
    }

    @BsonIgnore
    public PlayerProfileData createProfileForSlot(int slotNumber) {
        ensureProfileIntegrity();
        String profileId = PlayerProfileData.buildIdForSlot(slotNumber);

        for (PlayerProfileData profile : profiles) {
            if (profile.getProfileId().equals(profileId)) {
                return profile;
            }
        }

        PlayerProfileData profile = new PlayerProfileData(profileId);
        profiles.add(profile);
        return profile;
    }

    @BsonIgnore
    public boolean deleteProfile(String profileId) {
        ensureProfileIntegrity();

        if (profiles.size() <= 1) {
            return false;
        }

        boolean removed = profiles.removeIf(profile -> profile.getProfileId().equals(profileId));
        if (!removed) {
            return false;
        }

        if (profileId.equals(activeProfileId)) {
            activeProfileId = profiles.getFirst().getProfileId();
        }

        return true;
    }

    @BsonIgnore
    public int getMaxHealth() {
        return getActiveProfile().getMaxHealth();
    }

    @BsonIgnore
    public void setMaxHealth(int maxHealth) {
        getActiveProfile().setMaxHealth(maxHealth);
    }

    @BsonIgnore
    public int getMaxMana() {
        return getActiveProfile().getMaxMana();
    }

    @BsonIgnore
    public void setMaxMana(int maxMana) {
        getActiveProfile().setMaxMana(maxMana);
    }

    @BsonIgnore
    public BigInteger getCurrentExp() {
        return getActiveProfile().getCurrentExp();
    }

    @BsonIgnore
    public void setCurrentExp(BigInteger currentExp) {
        getActiveProfile().setCurrentExp(currentExp);
    }

    @BsonIgnore
    public long getPurse() {
        return getActiveProfile().getPurse();
    }

    @BsonIgnore
    public void setPurse(long purse) {
        getActiveProfile().setPurse(purse);
    }

    @BsonIgnore
    public long getBank() {
        return getActiveProfile().getBank();
    }

    @BsonIgnore
    public void setBank(long bank) {
        getActiveProfile().setBank(bank);
    }

    @BsonIgnore
    public long getRiftGold() {
        return getActiveProfile().getRiftGold();
    }

    @BsonIgnore
    public void setRiftGold(long riftGold) {
        getActiveProfile().setRiftGold(riftGold);
    }

    @BsonIgnore
    public long getUpgradePoints() {
        return getActiveProfile().getUpgradePoints();
    }

    @BsonIgnore
    public void setUpgradePoints(long upgradePoints) {
        getActiveProfile().setUpgradePoints(upgradePoints);
    }

    @BsonIgnore
    public int getAccessorySlots() {
        return getActiveProfile().getAccessorySlots();
    }

    @BsonIgnore
    public void setAccessorySlots(int accessorySlots) {
        getActiveProfile().setAccessorySlots(accessorySlots);
    }

    @BsonIgnore
    public List<String> getAccessoryBag() {
        return getActiveProfile().getAccessoryIds();
    }

    @BsonIgnore
    public void setAccessoryBag(List<String> accessoryBag) {
        getActiveProfile().setAccessoryIds(accessoryBag != null ? accessoryBag : new ArrayList<>());
    }

    @BsonIgnore
    public AccessoryBag getAccessoryBagObject() {
        return getActiveProfile().getAccessoryBagObject();
    }

    @BsonIgnore
    public List<String> getUnlockedLocationIds() {
        return getActiveProfile().getUnlockedLocationIds();
    }

    @BsonIgnore
    public void setUnlockedLocationIds(List<String> unlockedLocationIds) {
        getActiveProfile().setUnlockedLocationIds(unlockedLocationIds != null ? unlockedLocationIds : new ArrayList<>());
    }
}
