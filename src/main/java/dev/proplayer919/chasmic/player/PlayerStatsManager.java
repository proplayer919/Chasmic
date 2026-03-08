package dev.proplayer919.chasmic.player;

import dev.proplayer919.chasmic.items.CustomItem;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.item.ItemStack;

import java.util.*;

/**
 * Manages player stats, including health, mana, and stat bonuses
 */
public class PlayerStatsManager {
    private final CustomPlayer player;

    @Setter
    @Getter
    private int customHealth = 100;

    @Setter
    @Getter
    private int customMana = 100;

    private final Map<String, TempStatBonus> tempStatBonuses = new HashMap<>();

    // Cached stat values with dirty flags
    private final Map<PlayerStat, Float> cachedStats = new EnumMap<>(PlayerStat.class);
    private final Map<PlayerStat, Boolean> statsDirty = new EnumMap<>(PlayerStat.class);

    public PlayerStatsManager(CustomPlayer player) {
        this.player = player;
        // Initialize all stats as dirty
        for (PlayerStat stat : PlayerStat.values()) {
            statsDirty.put(stat, true);
        }
    }

    public int getMaxCustomHealth() {
        return player.getPlayerData() != null ? player.getPlayerData().getMaxHealth() : 100;
    }

    public int getMaxCustomMana() {
        return player.getPlayerData() != null ? player.getPlayerData().getMaxMana() : 100;
    }

    public void addTemporaryStatBonus(PlayerStat stat, float bonusAmount, long durationMillis, Runnable onExpire) {
        String uuid = UUID.randomUUID().toString();
        TempStatBonus tempBonus = new TempStatBonus(stat, bonusAmount);
        tempStatBonuses.put(uuid, tempBonus);
        markStatDirty(stat); // Mark dirty when adding so cache is invalidated

        // Schedule removal
        net.minestom.server.MinecraftServer.getSchedulerManager().buildTask(() -> {
            tempStatBonuses.remove(uuid);
            markStatDirty(stat);
            if (onExpire != null) {
                onExpire.run();
            }
        }).delay(durationMillis, java.time.temporal.ChronoUnit.MILLIS).schedule();
    }

    public float getStatFor(PlayerStat stat) {
        // Return cached value if not dirty
        if (!statsDirty.getOrDefault(stat, true)) {
            return cachedStats.getOrDefault(stat, 0f);
        }

        // Recalculate
        float baseValue = PlayerStatConstants.getBaseValue(stat);
        if (player.getPlayerData() == null) {
            return baseValue;
        }

        float bonus = player.getPlayerData().getAccessoryBagObject().sumStats().getOrDefault(stat, 0f);

        // Main hand bonuses
        for (PlayerStatBonus statBonus : getMainHandItemStatBonuses()) {
            if (statBonus.stat() == stat) {
                bonus += statBonus.bonusAmount();
            }
        }

        // Armor bonuses
        for (PlayerStatBonus statBonus : getArmorStatBonuses()) {
            if (statBonus.stat() == stat) {
                bonus += statBonus.bonusAmount();
            }
        }

        // Temporary bonuses
        for (TempStatBonus tempBonus : tempStatBonuses.values()) {
            if (tempBonus.stat() == stat) {
                bonus += tempBonus.bonus();
            }
        }

        float finalValue = baseValue + bonus;

        // Cache the result
        cachedStats.put(stat, finalValue);
        statsDirty.put(stat, false);

        return finalValue;
    }

    public void markStatDirty(PlayerStat stat) {
        statsDirty.put(stat, true);
    }

    public void markAllStatsDirty() {
        for (PlayerStat stat : PlayerStat.values()) {
            statsDirty.put(stat, true);
        }
    }

    private Collection<PlayerStatBonus> getMainHandItemStatBonuses() {
        ItemStack mainHandItem = player.getItemInMainHand();
        if (!mainHandItem.isAir()) {
            CustomItem customItem = CustomItem.getCustomItemFromItemStack(mainHandItem);
            if (customItem != null) {
                return customItem.getStatBonuses();
            }
        }
        return Collections.emptyList();
    }

    private Collection<PlayerStatBonus> getArmorStatBonuses() {
        List<PlayerStatBonus> statBonuses = new ArrayList<>();
        List<ItemStack> armorItems = List.of(player.getHelmet(), player.getChestplate(), player.getLeggings(), player.getBoots());
        for (ItemStack armorItem : armorItems) {
            if (!armorItem.isAir()) {
                CustomItem customItem = CustomItem.getCustomItemFromItemStack(armorItem);
                if (customItem != null) {
                    statBonuses.addAll(customItem.getStatBonuses());
                }
            }
        }
        return statBonuses;
    }
}

