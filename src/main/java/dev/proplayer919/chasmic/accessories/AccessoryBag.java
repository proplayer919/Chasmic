package dev.proplayer919.chasmic.accessories;

import dev.proplayer919.chasmic.player.PlayerStatBonus;
import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.player.PlayerStat;

import java.util.*;

public record AccessoryBag(Collection<Accessory> accessories) {
    public AccessoryBag() {
        this(new ArrayList<>());
    }

    public List<String> serialize() {
        List<String> serialized = new ArrayList<>();
        for (Accessory accessory : accessories) {
            serialized.add(accessory.getId());
        }
        return serialized;
    }

    public void deserialize(List<String> accessoryIds) {
        for (String id : accessoryIds) {
            Accessory accessory = Main.getAccessoryRegistry().getAccessoryById(id);
            if (accessory != null) {
                accessories.add(accessory);
            }
        }
    }

    public void addAccessory(Accessory accessory) {
        this.accessories.add(accessory);
    }

    public void removeAccessory(Accessory accessory) {
        this.accessories.remove(accessory);
    }

    public Map<PlayerStat, Float> sumStats() {
        Map<PlayerStat, Float> stats = new HashMap<>();
        for (Accessory accessory : accessories) {
            for (PlayerStatBonus statBonus : accessory.getStatsBonuses()) {
                stats.put(statBonus.stat(), stats.getOrDefault(statBonus.stat(), 0f) + statBonus.bonusAmount());
            }
        }
        return stats;
    }
}
