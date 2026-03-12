package dev.proplayer919.chasmic.data;

import dev.proplayer919.chasmic.accessories.AccessoryBag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Profile-scoped progression data for a player.
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerProfileData {
    public static final int TRACKED_SLOT_MIN = 0;
    public static final int TRACKED_SLOT_MAX = 40;
    public static final int MENU_SLOT = 8;

    @BsonProperty("id")
    private String profileId;

    @BsonProperty("currentExp")
    private BigInteger currentExp = BigInteger.ZERO;

    @BsonProperty("purse")
    private long purse = 0;

    @BsonProperty("bank")
    private long bank = 0;

    @BsonProperty("riftGold")
    private long riftGold = 0;

    @BsonProperty("upgradePoints")
    private long upgradePoints = 0;

    @BsonProperty("accessorySlots")
    private int accessorySlots = 3;

    @BsonProperty("accessoryBag")
    private List<String> accessoryIds = new ArrayList<>();

    @BsonProperty("unlockedLocations")
    private List<String> unlockedLocationIds = new ArrayList<>();

    // Stores only custom/accessory items by slot for this profile.
    @BsonProperty("inventoryItems")
    private List<InventoryItemData> inventoryItems = new ArrayList<>();

    public PlayerProfileData(String profileId) {
        this.profileId = profileId;
        this.unlockedLocationIds = new ArrayList<>();
        this.unlockedLocationIds.add("chasmic_city");
        this.inventoryItems = new ArrayList<>();
    }

    public static String buildIdForSlot(int slotNumber) {
        return "profile_" + slotNumber;
    }

    @BsonIgnore
    public AccessoryBag getAccessoryBagObject() {
        AccessoryBag bag = new AccessoryBag();
        if (accessoryIds != null) {
            bag.deserialize(accessoryIds);
        }
        return bag;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItemData {
        @BsonProperty("slot")
        private int slot;

        @BsonProperty("itemId")
        private String itemId;

        @BsonProperty("amount")
        private int amount;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof InventoryItemData other)) {
                return false;
            }
            return slot == other.slot && amount == other.amount && itemId.equals(other.itemId);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(slot, itemId, amount);
        }
    }
}
