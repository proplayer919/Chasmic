package dev.proplayer919.chasmic.location;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LocationRegistry {
    private final Map<String, Location> locations = new HashMap<>();

    public LocationRegistry() {
        registerLocation(new Location(null, "chasmic_city", "Chasmic City"));
    }

    public void registerLocation(Location location) {
        locations.put(location.id(), location);
    }

    public Location getLocation(String id) {
        return locations.get(id);
    }

    public Collection<Location> getLocations() {
        return locations.values();
    }
}
