package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.Main;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;

import java.util.HashMap;
import java.util.Map;

public class CustomItemRegistry {
    private final Map<String, CustomItem> items = new HashMap<>();

    public CustomItemRegistry() {
        // Register items here
        registerItem(new CustomItem("aspect_of_the_shallows",
                Component.text("Aspect of the Shallows"),
                Material.GOLDEN_SWORD,
                Component.text("A powerful magical weapon that can teleport the wielder quickly away from danger."),
                ItemType.WEAPON_MELEE,
                6.0f,
                Main.getItemActionRegistry().getItemAction("warp")));
    }

    public void registerItem(CustomItem item) {
        items.put(item.getId(), item);
    }

    public CustomItem getItem(String id) {
        return items.get(id);
    }
}
