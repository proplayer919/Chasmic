package dev.proplayer919.chasmic.accessories;

import dev.proplayer919.chasmic.PlayerStatBonus;
import dev.proplayer919.chasmic.Rarity;

import java.util.Collection;

public record Accessory(String id, String name, String description, Rarity rarity,
                        Collection<PlayerStatBonus> statsBonuses) {
}
