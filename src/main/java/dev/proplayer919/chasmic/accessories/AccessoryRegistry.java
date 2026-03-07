package dev.proplayer919.chasmic.accessories;

import dev.proplayer919.chasmic.accessories.types.DolphinAmulet;
import dev.proplayer919.chasmic.accessories.types.ImmaterialBand;
import dev.proplayer919.chasmic.accessories.types.TurtleSprite;
import dev.proplayer919.chasmic.accessories.types.WheatSacrifice;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccessoryRegistry {
    private final Map<String, Accessory> accessoryMap = new HashMap<>();

    public AccessoryRegistry() {
        registerAccessory(new ImmaterialBand());
        registerAccessory(new TurtleSprite());
        registerAccessory(new DolphinAmulet());
        registerAccessory(new WheatSacrifice());
    }

    public Accessory getAccessoryById(String id) {
        return accessoryMap.get(id);
    }

    public void registerAccessory(Accessory accessory) {
        accessoryMap.put(accessory.getId(), accessory);
    }

    public Collection<Accessory> getAllAccessories() {
        return accessoryMap.values();
    }
}
