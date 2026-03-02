package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.*;
import net.minestom.server.color.DyeColor;
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
                Rarity.LEGENDARY,
                new ArrayList<>(List.of(new PlayerStatBonus(PlayerStat.ATTACK, 5.0f))),
                Main.getItemActionRegistry().getItemAction("warp")));

        registerItem(new CustomItem("bane_of_nella",
                "Bane of Nella",
                Material.NETHERITE_SWORD,
                "A powerful melee weapon that deals extra damage and gives a higher chance for critical hits.",
                ItemType.WEAPON_MELEE,
                Rarity.LEGENDARY,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.ATTACK, 200.0f),
                        new PlayerStatBonus(PlayerStat.CRITICAL_CHANCE, 14.0f)
                ))));

        registerItem(new CustomItem("harvester_helmet_common",
                "Harvester Helmet",
                Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                Rarity.COMMON,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.FARMING, 1.0f),
                        new PlayerStatBonus(PlayerStat.DEFENSE, 5.0f)
                ))).withPlayerHeadTexture(PlayerHeads.HARVESTER_COMMON));

        registerItem(new CustomItem("harvester_helmet_uncommon",
                "Harvester Helmet",
                Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                Rarity.UNCOMMON,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.FARMING, 2.5f),
                        new PlayerStatBonus(PlayerStat.DEFENSE, 10.0f)
                ))).withPlayerHeadTexture(PlayerHeads.HARVESTER_UNCOMMON));

        registerItem(new CustomItem("harvester_helmet_rare",
                "Harvester Helmet",
                Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                Rarity.RARE,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.FARMING, 5.0f),
                        new PlayerStatBonus(PlayerStat.DEFENSE, 20.0f)
                ))).withPlayerHeadTexture(PlayerHeads.HARVESTER_RARE));

        registerItem(new CustomItem("harvester_helmet_epic",
                "Harvester Helmet",
                Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                Rarity.EPIC,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.FARMING, 10.0f),
                        new PlayerStatBonus(PlayerStat.DEFENSE, 35.0f)
                ))).withPlayerHeadTexture(PlayerHeads.HARVESTER_EPIC));

        registerItem(new CustomItem("harvester_helmet_legendary",
                "Harvester Helmet",
                Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                Rarity.LEGENDARY,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.FARMING, 20.0f),
                        new PlayerStatBonus(PlayerStat.DEFENSE, 55.0f)
                ))).withPlayerHeadTexture(PlayerHeads.HARVESTER_LEGENDARY));

        registerItem(new CustomItem("harvester_helmet_mythic",
                "Harvester Helmet",
                Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                Rarity.MYTHIC,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.FARMING, 35.0f),
                        new PlayerStatBonus(PlayerStat.DEFENSE, 80.0f)
                ))).withPlayerHeadTexture(PlayerHeads.HARVESTER_MYTHIC));

        registerItem(new CustomItem("volcanic_helmet",
                "Volcanic Helmet",
                Material.LEATHER_HELMET,
                "A crimson helmet that provides a significant boost to defense.",
                ItemType.ARMOR,
                Rarity.RARE,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.DEFENSE, 25.0f)
                ))).withLeatherColor(DyeColor.RED.color()));

        registerItem(new CustomItem("volcanic_chestplate",
                "Volcanic Chestplate",
                Material.LEATHER_CHESTPLATE,
                "A crimson chestplate that provides a significant boost to defense.",
                ItemType.ARMOR,
                Rarity.RARE,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.DEFENSE, 25.0f)
                ))).withLeatherColor(DyeColor.RED.color()));

        registerItem(new CustomItem("volcanic_leggings",
                "Volcanic Leggings",
                Material.LEATHER_LEGGINGS,
                "Crimson leggings that provides a significant boost to defense.",
                ItemType.ARMOR,
                Rarity.RARE,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.DEFENSE, 25.0f)
                ))).withLeatherColor(DyeColor.RED.color()));

        registerItem(new CustomItem("volcanic_boots",
                "Volcanic Boots",
                Material.LEATHER_BOOTS,
                "Crimson boots that provides a significant boost to defense.",
                ItemType.ARMOR,
                Rarity.RARE,
                new ArrayList<>(List.of(
                        new PlayerStatBonus(PlayerStat.DEFENSE, 25.0f)
                ))).withLeatherColor(DyeColor.RED.color()));
    }

    public void registerItem(CustomItem item) {
        items.put(item.getId(), item);
    }

    public CustomItem getItem(String id) {
        return items.get(id);
    }

    public Collection<CustomItem> getAllItems() {
        return items.values();
    }
}
