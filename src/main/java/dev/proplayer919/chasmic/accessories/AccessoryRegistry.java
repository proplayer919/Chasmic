package dev.proplayer919.chasmic.accessories;

import dev.proplayer919.chasmic.accessories.types.ImmaterialBand;

import java.util.HashMap;
import java.util.Map;

public class AccessoryRegistry {
    private final Map<String, Accessory> accessoryMap = new HashMap<>();

    public AccessoryRegistry() {
        registerAccessory(new ImmaterialBand());
    }

    public Accessory getAccessoryById(String id) {
        return accessoryMap.get(id);
    }

    public void registerAccessory(Accessory accessory) {
        accessoryMap.put(accessory.getId(), accessory);
    }
}
