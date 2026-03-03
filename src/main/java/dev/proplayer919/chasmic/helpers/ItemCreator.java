package dev.proplayer919.chasmic.helpers;

import dev.proplayer919.chasmic.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.items.ItemAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Food;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static dev.proplayer919.chasmic.items.CustomItem.itemActionTag;
import static dev.proplayer919.chasmic.items.CustomItem.itemIdTag;

public abstract class ItemCreator {
    public static final Tag<String> itemUuidTag = Tag.String("custom_item_uuid");

    public static ItemStack createItem(int amount, Material material, String id, String name, String description, Rarity rarity, Collection<PlayerStatBonus> statBonuses, String playerHeadTexture, Color leatherColor, ItemAction action, boolean edible) {
        ItemStack.Builder builder;
        if (playerHeadTexture != null) {
            builder = PlayerHeadCreator.getHeadBuilder(playerHeadTexture);
        } else {
            builder = ItemStack.builder(material);
        }

        if (leatherColor != null) {
            builder.set(DataComponents.DYED_COLOR, leatherColor);
        }

        if (edible) {
            builder.set(DataComponents.FOOD, new Food(0, 0f, true));
        }

        // Build lore
        List<Component> lore = new ArrayList<>(wrapText(description, 40));

        // Add blank line if there are stat bonuses
        if (!statBonuses.isEmpty()) {
            lore.add(Component.empty());
        }

        // Add stat bonuses
        for (PlayerStatBonus bonus : statBonuses) {
            String sign = bonus.bonusAmount() >= 0 ? "+" : "";
            Component bonusLine = Component.text(bonus.stat().getStatIcon() + " " + sign + formatStatValue(bonus.bonusAmount()) + " " + bonus.stat().getStatName())
                    .decoration(TextDecoration.ITALIC, false)
                    .color(bonus.stat().getStatColor());
            lore.add(bonusLine);
        }

        // Add blank line before rarity
        lore.add(Component.empty());

        // Add rarity
        Component rarityLine = Component.text(rarity.getRarityName().toUpperCase())
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true)
                .color(rarity.getColor());
        lore.add(rarityLine);

        builder.set(DataComponents.ITEM_NAME, Component.text(name).color(rarity.getColor()))
                .lore(lore)
                .set(itemIdTag, id);

        builder.set(itemUuidTag, UUID.randomUUID().toString());

        if (action != null) {
            builder.set(itemActionTag, action.id());
        }

        builder.amount(amount);

        return builder.build();
    }

    public static List<Component> wrapText(String text, int maxLineLength) {
        List<Component> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                if (!currentLine.isEmpty()) {
                    lines.add(Component.text(currentLine.toString())
                            .decoration(TextDecoration.ITALIC, false)
                            .color(NamedTextColor.GRAY));
                    currentLine = new StringBuilder();
                }
            }

            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        if (!currentLine.isEmpty()) {
            lines.add(Component.text(currentLine.toString())
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.GRAY));
        }

        return lines;
    }

    public static String formatStatValue(float value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.1f", value);
        }
    }
}
