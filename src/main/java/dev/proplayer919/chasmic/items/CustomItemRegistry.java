package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.*;
import net.minestom.server.color.Color;
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

        registerItem(new CustomItem("bagel",
                "Bagel",
                Material.PLAYER_HEAD,
                "A delicious bagel that restores health when consumed.",
                ItemType.FOOD,
                Rarity.COMMON,
                new ArrayList<>(List.of())).withHealthRestoration(20).withPlayerHeadTexture(PlayerHeads.BAGEL));

        registerItemWithRarities("harvester_helmet", "Harvester Helmet", Material.IRON_HELMET,
                "A sturdy helmet that provides protection for the head and farming bonuses.",
                ItemType.ARMOR,
                List.of(
                        new RarityVariant(Rarity.COMMON, List.of(
                                new PlayerStatBonus(PlayerStat.FARMING, 1.0f),
                                new PlayerStatBonus(PlayerStat.DEFENSE, 5.0f)
                        ), PlayerHeads.HARVESTER_COMMON),
                        new RarityVariant(Rarity.UNCOMMON, List.of(
                                new PlayerStatBonus(PlayerStat.FARMING, 2.5f),
                                new PlayerStatBonus(PlayerStat.DEFENSE, 10.0f)
                        ), PlayerHeads.HARVESTER_UNCOMMON),
                        new RarityVariant(Rarity.RARE, List.of(
                                new PlayerStatBonus(PlayerStat.FARMING, 5.0f),
                                new PlayerStatBonus(PlayerStat.DEFENSE, 20.0f)
                        ), PlayerHeads.HARVESTER_RARE),
                        new RarityVariant(Rarity.EPIC, List.of(
                                new PlayerStatBonus(PlayerStat.FARMING, 10.0f),
                                new PlayerStatBonus(PlayerStat.DEFENSE, 35.0f)
                        ), PlayerHeads.HARVESTER_EPIC),
                        new RarityVariant(Rarity.LEGENDARY, List.of(
                                new PlayerStatBonus(PlayerStat.FARMING, 20.0f),
                                new PlayerStatBonus(PlayerStat.DEFENSE, 55.0f)
                        ), PlayerHeads.HARVESTER_LEGENDARY),
                        new RarityVariant(Rarity.MYTHIC, List.of(
                                new PlayerStatBonus(PlayerStat.FARMING, 35.0f),
                                new PlayerStatBonus(PlayerStat.DEFENSE, 80.0f)
                        ), PlayerHeads.HARVESTER_MYTHIC)
                ));

        registerArmorSet("volcanic", "Crimson armor that provides a significant boost to defense.", Rarity.RARE, new ArrayList<>(List.of(
                new PlayerStatBonus(PlayerStat.DEFENSE, 25.0f)
        )), ArmorTier.LEATHER, DyeColor.RED.color());

        registerArmorSet("abyssal", "Abyssal armor that provides a significant boost to defense.", Rarity.EPIC, new ArrayList<>(List.of(
                new PlayerStatBonus(PlayerStat.DEFENSE, 35.0f)
        )), ArmorTier.LEATHER, DyeColor.BLUE.color());
    }

    public void registerItem(CustomItem item) {
        items.put(item.getId(), item);
    }

    public void registerArmorSet(String adjective, String description, Rarity rarity, Collection<PlayerStatBonus> bonuses, ArmorTier armorTier, Color leatherColor) {
        String adjectiveLower = adjective.toLowerCase();
        String adjectiveTitle = capitalizeString(adjective);

        List<Material> materials = getMaterialsForTier(armorTier);
        String[] pieceNames = {"helmet", "chestplate", "leggings", "boots"};

        for (int i = 0; i < pieceNames.length; i++) {
            String pieceName = pieceNames[i];
            CustomItem armorPiece = new CustomItem(
                    adjectiveLower + "_" + pieceName,
                    adjectiveTitle + " " + capitalizeString(pieceName),
                    materials.get(i),
                    description,
                    ItemType.ARMOR,
                    rarity,
                    bonuses
            ).withLeatherColor(leatherColor);
            registerItem(armorPiece);
        }
    }

    private String capitalizeString(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private List<Material> getMaterialsForTier(ArmorTier armorTier) {
        return switch (armorTier) {
            case LEATHER -> List.of(Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS);
            case CHAINMAIL -> List.of(Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS);
            case IRON -> List.of(Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS);
            case GOLD -> List.of(Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS);
            case DIAMOND -> List.of(Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);
            case NETHERITE -> List.of(Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);
        };
    }

    private void registerItemWithRarities(String baseName, String displayName, Material material, String description, ItemType type, List<RarityVariant> variants) {
        for (RarityVariant variant : variants) {
            String id = baseName + "_" + variant.rarity().name().toLowerCase();
            CustomItem item = new CustomItem(
                    id,
                    displayName,
                    material,
                    description,
                    type,
                    variant.rarity(),
                    variant.bonuses()
            );

            if (variant.playerHeadTexture() != null) {
                item = item.withPlayerHeadTexture(variant.playerHeadTexture());
            }

            registerItem(item);
        }
    }

    private record RarityVariant(Rarity rarity, Collection<PlayerStatBonus> bonuses, String playerHeadTexture) {
        public RarityVariant(Rarity rarity, Collection<PlayerStatBonus> bonuses) {
            this(rarity, bonuses, null);
        }
    }

    public CustomItem getItem(String id) {
        return items.get(id);
    }

    public Collection<CustomItem> getAllItems() {
        return items.values();
    }
}
