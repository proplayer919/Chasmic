package dev.proplayer919.chasmic.accessories;

import dev.proplayer919.chasmic.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.helpers.ItemCreator;
import lombok.Getter;
import net.minestom.server.color.Color;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.Collection;

@Getter
public class Accessory {
    private final String id;
    private final String name;
    private final String description;
    private final Rarity rarity;
    private final Collection<PlayerStatBonus> statsBonuses;
    private final Material material;
    private String playerHeadTexture;
    private Color leatherColor;

    public Accessory(String id, String name, String description, Rarity rarity,
                     Collection<PlayerStatBonus> statsBonuses, Material material) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.statsBonuses = statsBonuses;
        this.material = material;
    }

    public Accessory(String id, String name, String description, Rarity rarity,
                     Collection<PlayerStatBonus> statsBonuses, Material material, String playerHeadTexture) {
        this(id, name, description, rarity, statsBonuses, material);
        this.playerHeadTexture = playerHeadTexture;
    }

    public ItemStack getItemStack(int amount) {
        return ItemCreator.createItem(amount, material, id, name, description, rarity, statsBonuses, playerHeadTexture, leatherColor, null);
    }

    public ItemStack getItemStack() {
        return getItemStack(1);
    }
}
