package dev.proplayer919.chasmic.gui;

import dev.proplayer919.chasmic.helpers.ItemCreator;
import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

public record GuiItem(Material material, Component displayName, String description, GuiClickAction clickAction) {

    public ItemStack getItemStack() {
        ItemStack.Builder itemBuilder = ItemStack.builder(material);

        itemBuilder.set(DataComponents.ITEM_NAME, displayName);

        if (description != null && !description.isEmpty()) {
            itemBuilder.lore(ItemCreator.wrapText(description, 40));
        }

        return itemBuilder.build();
    }
}
