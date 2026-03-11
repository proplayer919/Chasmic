package dev.proplayer919.chasmic.player.gui;

import dev.proplayer919.chasmic.helpers.ItemCreator;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public class GuiItem {
    private final Material material;
    private final Component displayName;
    private final List<Component> description;
    private final GuiClickAction leftClickAction;
    private final GuiClickAction rightClickAction;
    private final ItemStack customItemStack;

    public GuiItem(Material material, Component displayName, List<Component> description, GuiClickAction clickAction) {
        this(material, displayName, description, clickAction, null);
    }

    public GuiItem(Material material, Component displayName, List<Component> description, GuiClickAction leftClickAction, GuiClickAction rightClickAction) {
        this.material = material;
        this.displayName = displayName;
        this.description = description;
        this.leftClickAction = leftClickAction;
        this.rightClickAction = rightClickAction;
        this.customItemStack = null;
    }

    public GuiItem(Material material, Component displayName, String description, GuiClickAction clickAction) {
        this(material, displayName, ItemCreator.wrapText(description, 40), clickAction, null);
    }

    public GuiItem(ItemStack itemStack, GuiClickAction clickAction) {
        this(itemStack, clickAction, null);
    }

    public GuiItem(ItemStack itemStack, GuiClickAction leftClickAction, GuiClickAction rightClickAction) {
        this.material = null;
        this.displayName = null;
        this.description = null;
        this.leftClickAction = leftClickAction;
        this.rightClickAction = rightClickAction;
        this.customItemStack = itemStack;
    }

    public GuiClickAction clickAction() {
        return leftClickAction;
    }

    public GuiClickAction leftClickAction() {
        return leftClickAction;
    }

    public GuiClickAction rightClickAction() {
        return rightClickAction;
    }

    public ItemStack getItemStack() {
        if (customItemStack != null) {
            return customItemStack;
        }

        ItemStack.Builder itemBuilder = ItemStack.builder(material);

        itemBuilder.set(DataComponents.ITEM_NAME, displayName);

        if (description != null && !description.isEmpty()) {
            itemBuilder.lore(description);
        }

        return itemBuilder.build();
    }
}
