package dev.proplayer919.chasmic;

import lombok.Getter;

@Getter
public enum Rarity {
    COMMON("common"),
    UNCOMMON("uncommon"),
    RARE("rare"),
    EPIC("epic"),
    LEGENDARY("legendary"),
    MYTHIC("mythic");

    private final String rarityName;

    Rarity(String rarityName) {
        this.rarityName = rarityName;
    }
}
