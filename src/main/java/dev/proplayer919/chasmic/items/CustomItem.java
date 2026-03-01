package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.helpers.PlayerHeadCreator;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.color.Color;
import net.minestom.server.color.DyeColor;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomItem {
    private final String id;
    private final String displayName;
    private final Material material;
    private final String description;
    private final ItemType itemType;
    private final Rarity rarity;
    private final Collection<PlayerStatBonus> statBonuses;
    private ItemAction action;
    private String playerHeadTexture; // Optional field for player head items
    private Color leatherColor; // Optional field for leather armor items

    public static final Tag<String> itemActionTag = Tag.String("custom_item_action");
    public static final Tag<String> itemIdTag = Tag.String("custom_item_id");

    public CustomItem(String id, String displayName, Material material, String description, ItemType itemType, Rarity rarity, Collection<PlayerStatBonus> statBonuses) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.description = description;
        this.itemType = itemType;
        this.rarity = rarity;
        this.statBonuses = statBonuses;
    }

    public CustomItem(String id, String displayName, Material material, String description, ItemType itemType, Rarity rarity, Collection<PlayerStatBonus> statBonuses, ItemAction action) {
        this(id, displayName, material, description, itemType, rarity, statBonuses);
        this.action = action;
    }

    public CustomItem withPlayerHeadTexture(String texture) {
        this.playerHeadTexture = texture;
        return this;
    }

    public CustomItem withLeatherColor(Color color) {
        this.leatherColor = color;
        return this;
    }

    public ItemStack getItemStack(int amount) {
        ItemStack.Builder builder;
        if (playerHeadTexture != null) {
            builder = PlayerHeadCreator.getHeadBuilder(playerHeadTexture);
        } else {
            builder = ItemStack.builder(material);
        }

        if (leatherColor != null) {
            builder.set(DataComponents.DYED_COLOR, leatherColor);
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

        builder.set(DataComponents.ITEM_NAME, Component.text(displayName).color(rarity.getColor()))
                .lore(lore)
                .set(itemIdTag, id);

        if (action != null) {
            builder.set(itemActionTag, action.id());
        }

        builder.amount(amount);

        return builder.build();
    }

    private List<Component> wrapText(String text, int maxLineLength) {
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

    private String formatStatValue(float value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.1f", value);
        }
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public static CustomItem getCustomItemFromItemStack(ItemStack itemStack) {
        String itemId = itemStack.getTag(itemIdTag);
        if (itemId == null) {
            return null;
        }
        return Main.getCustomItemRegistry().getItem(itemId);
    }

    public static String getItemId(ItemStack itemStack) {
        return itemStack.getTag(itemIdTag);
    }
}
