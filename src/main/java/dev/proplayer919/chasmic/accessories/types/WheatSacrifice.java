package dev.proplayer919.chasmic.accessories.types;

import dev.proplayer919.chasmic.PlayerHeads;
import dev.proplayer919.chasmic.Rarity;
import dev.proplayer919.chasmic.accessories.Accessory;
import dev.proplayer919.chasmic.player.PlayerStat;
import dev.proplayer919.chasmic.player.PlayerStatBonus;
import net.minestom.server.item.Material;

import java.util.List;

public class WheatSacrifice extends Accessory {
    public WheatSacrifice() {
        super("wheat_sacrifice", "Wheat Sacrifice", "A sacrifice of wheat to the farming gods, providing a boost across all areas of farming.", Rarity.UNCOMMON, List.of(new PlayerStatBonus(PlayerStat.FARMING, 15.0f)), Material.PLAYER_HEAD, PlayerHeads.WHEAT_BARREL);
    }
}
