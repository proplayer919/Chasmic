package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.PlayerStatBonus;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

import java.util.Collection;

@Getter
public class CustomItem {
    private final String id;
    private final String displayName;
    private final Material material;
    private final String description;
    private final ItemType itemType;
    private final Collection<PlayerStatBonus> statBonuses;

    private ItemAction action;
    public static final Tag<String> itemActionTag = Tag.String("custom_item_action");
    public static final Tag<String> itemIdTag = Tag.String("custom_item_id");

    public CustomItem(String id, String displayName, Material material, String description, ItemType itemType, Collection<PlayerStatBonus> statBonuses) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.description = description;
        this.itemType = itemType;
        this.statBonuses = statBonuses;
    }

    public CustomItem(String id, String displayName, Material material, String description, ItemType itemType, Collection<PlayerStatBonus> statBonuses, ItemAction action) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.description = description;
        this.itemType = itemType;
        this.statBonuses = statBonuses;
        this.action = action;
    }

    public ItemStack getItemStack() {
        ItemStack itemStack;

        if (action == null) {
            itemStack = ItemStack.builder(material)
                    .set(DataComponents.ITEM_NAME, Component.text(displayName))
                    .lore(Component.text(description).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE))
                    .set(itemIdTag, id)
                    .build();
        } else {
            itemStack = ItemStack.builder(material)
                    .set(DataComponents.ITEM_NAME, Component.text(displayName))
                    .lore(Component.text(description).decoration(TextDecoration.ITALIC, false).color(NamedTextColor.WHITE))
                    .set(itemActionTag, action.id())
                    .set(itemIdTag, id)
                    .build();
        }

        return itemStack;
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
