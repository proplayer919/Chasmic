package dev.proplayer919.chasmic.player.gui;

import net.kyori.adventure.text.Component;
import net.minestom.server.component.DataComponents;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.TooltipDisplay;

public abstract class GuiConstants {
    public static final ItemStack FILLER_MATERIAL = ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
            .set(DataComponents.ITEM_NAME, Component.empty())
            .set(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.EMPTY)
            .build();
}
