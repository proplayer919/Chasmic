package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.PlayerStat;
import dev.proplayer919.chasmic.PlayerStatBonus;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;

import java.util.*;

public class CustomItemRegistry {
    private final Map<String, CustomItem> items = new HashMap<>();

    public CustomItemRegistry() {
        // Register items here
        registerItem(new CustomItem("aspect_of_the_shallows",
                "Aspect of the Shallows",
                Material.GOLDEN_SWORD,
                "A powerful magical weapon that can teleport the wielder quickly away from danger.",
                ItemType.WEAPON_MELEE,
                new ArrayList<>(List.of(new PlayerStatBonus(PlayerStat.ATTACK, 5.0f))),
                Main.getItemActionRegistry().getItemAction("warp")));
    }

    public void registerItem(CustomItem item) {
        items.put(item.getId(), item);
    }

    public CustomItem getItem(String id) {
        return items.get(id);
    }
}
