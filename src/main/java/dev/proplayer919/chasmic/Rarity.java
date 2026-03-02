package dev.proplayer919.chasmic;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

@Getter
public enum Rarity {
    COMMON("common", NamedTextColor.WHITE),
    UNCOMMON("uncommon", NamedTextColor.GREEN),
    RARE("rare", NamedTextColor.BLUE),
    EPIC("epic", NamedTextColor.LIGHT_PURPLE),
    LEGENDARY("legendary", NamedTextColor.GOLD),
    MYTHIC("mythic", NamedTextColor.YELLOW),
    SPECIAL("special", NamedTextColor.RED);

    private final String rarityName;
    private final TextColor color;

    Rarity(String rarityName, TextColor color) {
        this.rarityName = rarityName;
        this.color = color;
    }
}
