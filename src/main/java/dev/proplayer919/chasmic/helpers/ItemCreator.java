package dev.proplayer919.chasmic.helpers;

import dev.proplayer919.chasmic.player.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.player.gui.GuiClickAction;
import dev.proplayer919.chasmic.player.gui.GuiItem;
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

public abstract class ItemCreator {
    public static final Tag<String> itemUuidTag = Tag.String("custom_item_uuid");
    public static final Tag<String> itemActionsTag = Tag.String("custom_item_action");
    public static final Tag<String> itemIdTag = Tag.String("custom_item_id");

    public static ItemStack createItem(int amount, Material material, String id, String name, String description, Rarity rarity, Collection<PlayerStatBonus> statBonuses, String playerHeadTexture, Color leatherColor, List<ItemAction> actions, boolean edible) {
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

        List<ItemAction> validActions = actions == null ? List.of() : actions.stream().filter(action -> action != null && action.id() != null).toList();

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

        if (!validActions.isEmpty()) {
            lore.add(Component.empty());
        }

        for (int i = 0; i < validActions.size(); i++) {
            ItemAction action = validActions.get(i);

            Component header = Component.text(action.actionType().getDisplayName())
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true)
                    .color(NamedTextColor.GOLD);
            lore.add(header);

            Component actionNameLine = Component.text(action.name())
                    .color(NamedTextColor.WHITE)
                    .decoration(TextDecoration.ITALIC, false);
            lore.add(actionNameLine);

            lore.addAll(wrapText(action.description(), 40));

            Component cooldownLine = Component.text("Cooldown: " + formatDouble(action.cooldownSeconds()) + "s")
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.DARK_GRAY);
            lore.add(cooldownLine);

            Component manaLine = Component.text("Mana Cost: " + action.manaCost())
                    .decoration(TextDecoration.ITALIC, false)
                    .color(NamedTextColor.DARK_AQUA);
            lore.add(manaLine);

            if (i < validActions.size() - 1) {
                lore.add(Component.empty());
            }
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

        if (!validActions.isEmpty()) {
            String actionsString = String.join(",", validActions.stream().map(ItemAction::id).toList());
            builder.set(itemActionsTag, actionsString);
        }

        builder.amount(amount);

        return builder.build();
    }

    public static List<Component> wrapText(String text, int maxLineLength) {
        List<Component> lines = new ArrayList<>();

        // Split by newlines first
        String[] paragraphs = text.split("\\n");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
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

    private static String formatDouble(double value) {
        if (value == (long) value) {
            return String.format("%d", (long) value);
        }
        return String.format("%.1f", value);
    }

    /**
     * Creates a GuiItem with a player head texture
     */
    public static GuiItem createGuiItemWithTexture(String playerHeadTexture, Component displayName, List<Component> description, GuiClickAction clickAction) {
        ItemStack.Builder builder = PlayerHeadCreator.getHeadBuilder(playerHeadTexture);
        builder.set(DataComponents.ITEM_NAME, displayName);

        if (description != null && !description.isEmpty()) {
            builder.set(DataComponents.LORE, description);
        }

        ItemStack itemStack = builder.build();
        return new GuiItem(itemStack, clickAction);
    }
}
