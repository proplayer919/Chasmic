package dev.proplayer919.chasmic.player;

import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.accessories.Accessory;
import dev.proplayer919.chasmic.accessories.AccessoryRegistry;
import dev.proplayer919.chasmic.data.MongoDBHandler;
import dev.proplayer919.chasmic.data.PlayerData;
import dev.proplayer919.chasmic.data.PlayerProfileData;
import dev.proplayer919.chasmic.items.CustomItem;
import dev.proplayer919.chasmic.items.CustomItemRegistry;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles profile-scoped inventory persistence for a player.
 */
public class PlayerProfileInventoryManager {
    private final CustomPlayer player;

    public PlayerProfileInventoryManager(CustomPlayer player) {
        this.player = player;
    }

    public boolean saveActiveProfileInventory() {
        PlayerData playerData = player.getPlayerData();
        if (playerData == null) {
            return false;
        }

        playerData.ensureProfileIntegrity();
        PlayerProfileData activeProfile = playerData.getActiveProfile();
        PlayerInventory inventory = player.getInventory();

        List<PlayerProfileData.InventoryItemData> snapshot = new ArrayList<>();

        for (int slot = PlayerProfileData.TRACKED_SLOT_MIN; slot <= PlayerProfileData.TRACKED_SLOT_MAX; slot++) {
            if (slot == PlayerProfileData.MENU_SLOT) {
                continue;
            }

            ItemStack stack = getTrackedSlotItem(inventory, slot);
            String itemId = CustomItem.getItemId(stack);
            if (itemId == null || !isProfilePersistedItemId(itemId)) {
                continue;
            }

            snapshot.add(new PlayerProfileData.InventoryItemData(slot, itemId, stack.amount()));
        }

        List<PlayerProfileData.InventoryItemData> existing = activeProfile.getInventoryItems();
        boolean changed = existing == null || !existing.equals(snapshot);
        if (changed) {
            activeProfile.setInventoryItems(snapshot);
        }

        return changed;
    }

    public void loadActiveProfileInventory() {
        PlayerData playerData = player.getPlayerData();
        if (playerData == null) {
            return;
        }

        playerData.ensureProfileIntegrity();
        PlayerProfileData activeProfile = playerData.getActiveProfile();
        PlayerInventory inventory = player.getInventory();

        Map<Integer, PlayerProfileData.InventoryItemData> entriesBySlot = new HashMap<>();
        if (activeProfile.getInventoryItems() != null) {
            for (PlayerProfileData.InventoryItemData entry : activeProfile.getInventoryItems()) {
                entriesBySlot.put(entry.getSlot(), entry);
            }
        }

        for (int slot = PlayerProfileData.TRACKED_SLOT_MIN; slot <= PlayerProfileData.TRACKED_SLOT_MAX; slot++) {
            if (slot == PlayerProfileData.MENU_SLOT) {
                continue;
            }

            PlayerProfileData.InventoryItemData entry = entriesBySlot.get(slot);
            if (entry != null) {
                ItemStack rebuilt = buildPersistedProfileItem(entry.getItemId(), entry.getAmount());
                if (rebuilt != null) {
                    setTrackedSlotItem(inventory, slot, rebuilt);
                    continue;
                }
            }

            ItemStack current = getTrackedSlotItem(inventory, slot);
            String currentItemId = CustomItem.getItemId(current);
            if (currentItemId != null && isProfilePersistedItemId(currentItemId)) {
                setTrackedSlotItem(inventory, slot, ItemStack.AIR);
            }
        }
    }

    public void persistActiveProfileInventoryIfChanged() {
        PlayerData playerData = player.getPlayerData();
        if (playerData == null || !saveActiveProfileInventory()) {
            return;
        }

        MongoDBHandler mongoDBHandler = Main.getServiceContainer().getMongoDBHandler();
        if (mongoDBHandler != null) {
            mongoDBHandler.savePlayerData(playerData);
        }
    }

    private ItemStack getTrackedSlotItem(PlayerInventory inventory, int slot) {
        return switch (slot) {
            case 36 -> inventory.getEquipment(EquipmentSlot.BOOTS, player.getHeldSlot());
            case 37 -> inventory.getEquipment(EquipmentSlot.LEGGINGS, player.getHeldSlot());
            case 38 -> inventory.getEquipment(EquipmentSlot.CHESTPLATE, player.getHeldSlot());
            case 39 -> inventory.getEquipment(EquipmentSlot.HELMET, player.getHeldSlot());
            case 40 -> inventory.getEquipment(EquipmentSlot.OFF_HAND, player.getHeldSlot());
            default -> inventory.getItemStack(slot);
        };
    }

    private void setTrackedSlotItem(PlayerInventory inventory, int slot, ItemStack stack) {
        switch (slot) {
            case 36 -> inventory.setEquipment(EquipmentSlot.BOOTS, player.getHeldSlot(), stack);
            case 37 -> inventory.setEquipment(EquipmentSlot.LEGGINGS, player.getHeldSlot(), stack);
            case 38 -> inventory.setEquipment(EquipmentSlot.CHESTPLATE, player.getHeldSlot(), stack);
            case 39 -> inventory.setEquipment(EquipmentSlot.HELMET, player.getHeldSlot(), stack);
            case 40 -> inventory.setEquipment(EquipmentSlot.OFF_HAND, player.getHeldSlot(), stack);
            default -> inventory.setItemStack(slot, stack);
        }
    }

    private boolean isProfilePersistedItemId(String itemId) {
        CustomItemRegistry customItemRegistry = Main.getServiceContainer().getCustomItemRegistry();
        AccessoryRegistry accessoryRegistry = Main.getServiceContainer().getAccessoryRegistry();
        return customItemRegistry.getItem(itemId) != null || accessoryRegistry.getAccessoryById(itemId) != null;
    }

    private ItemStack buildPersistedProfileItem(String itemId, int amount) {
        CustomItemRegistry customItemRegistry = Main.getServiceContainer().getCustomItemRegistry();
        AccessoryRegistry accessoryRegistry = Main.getServiceContainer().getAccessoryRegistry();

        int clampedAmount = Math.max(1, amount);

        CustomItem customItem = customItemRegistry.getItem(itemId);
        if (customItem != null) {
            return customItem.getItemStack(clampedAmount);
        }

        Accessory accessory = accessoryRegistry.getAccessoryById(itemId);
        if (accessory != null) {
            return accessory.getItemStack(clampedAmount);
        }

        return null;
    }
}

