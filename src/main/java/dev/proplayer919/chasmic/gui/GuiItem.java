package dev.proplayer919.chasmic.gui;

import dev.proplayer919.chasmic.helpers.ItemCreator;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;

public record GuiItem(Material material, Component displayName, List<Component> description,
                      GuiClickAction clickAction) {
    public GuiItem(Material material, Component displayName, String description, GuiClickAction clickAction) {
        this(material, displayName, ItemCreator.wrapText(description, 40), clickAction);
    }

    public ItemStack getItemStack() {
        ItemStack.Builder itemBuilder = ItemStack.builder(material);

        itemBuilder.set(DataComponents.ITEM_NAME, displayName);

        if (description != null && !description.isEmpty()) {
            itemBuilder.lore(description);
        }

        return itemBuilder.build();
    }
}
