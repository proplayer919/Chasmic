package dev.proplayer919.chasmic.accessories;

import dev.proplayer919.chasmic.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;
import lombok.Getter;

import java.util.Collection;

@Getter
public class Accessory {
    private final String id;
    private final String name;
    private final String description;
    private final Rarity rarity;
    private final Collection<PlayerStatBonus> statsBonuses;

    public Accessory(String id, String name, String description, Rarity rarity,
                     Collection<PlayerStatBonus> statsBonuses) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rarity = rarity;
        this.statsBonuses = statsBonuses;
    }
}
