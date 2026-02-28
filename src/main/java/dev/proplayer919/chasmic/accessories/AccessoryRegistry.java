package dev.proplayer919.chasmic.accessories;

import java.util.Map;

public class AccessoryRegistry {
    private Map<String, Accessory> accessoryMap;

    public AccessoryRegistry() {

    }

    public Accessory getAccessoryById(String id) {
        return accessoryMap.get(id);
    }

    public void registerAccessory(Accessory accessory) {
        accessoryMap.put(accessory.id(), accessory);
    }
}
