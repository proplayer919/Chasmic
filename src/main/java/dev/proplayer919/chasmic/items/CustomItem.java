package dev.proplayer919.chasmic.items;

import dev.proplayer919.chasmic.Main;
import dev.proplayer919.chasmic.player.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.helpers.ItemCreator;
import lombok.Getter;
import net.minestom.server.color.Color;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;

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
    private int healthRestoration; // Amount of health restored if edible
    private List<ItemAction> actions; // List of actions that can be performed with this item
    private String playerHeadTexture; // Optional field for player head items
    private Color leatherColor; // Optional field for leather armor items

    public static final Tag<String> itemActionsTag = Tag.String("custom_item_action");
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

    public CustomItem(String id, String displayName, Material material, String description, ItemType itemType, Rarity rarity, Collection<PlayerStatBonus> statBonuses, List<ItemAction> actions) {
        this(id, displayName, material, description, itemType, rarity, statBonuses);
        this.actions = actions;
    }

    public CustomItem withPlayerHeadTexture(String texture) {
        this.playerHeadTexture = texture;
        return this;
    }

    public CustomItem withLeatherColor(Color color) {
        this.leatherColor = color;
        return this;
    }

    public CustomItem withHealthRestoration(int healthRestoration) {
        this.healthRestoration = healthRestoration;
        return this;
    }

    public ItemStack getItemStack(int amount) {
        return ItemCreator.createItem(amount, material, id, displayName, description, rarity, statBonuses, playerHeadTexture, leatherColor, actions, itemType == ItemType.FOOD);
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }

    public static CustomItem getCustomItemFromItemStack(ItemStack itemStack) {
        String itemId = itemStack.getTag(itemIdTag);
        if (itemId == null) {
            return null;
        }
        return Main.getServiceContainer().getCustomItemRegistry().getItem(itemId);
    }

    public static String getItemId(ItemStack itemStack) {
        return itemStack.getTag(itemIdTag);
    }
}
